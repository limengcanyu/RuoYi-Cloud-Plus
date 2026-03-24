package org.dromara.common.http.registrar;

import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.core.annotation.RemoteHttpService;
import org.dromara.common.http.annotation.RemoteServiceController;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.env.Environment;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.ClassUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.registry.AbstractHttpServiceRegistrar;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 按接口声明自动注册远程 HTTP Service.
 *
 * 这个注册器负责把“接口声明”转成 Spring HTTP Service Client 代理，
 * 同时保留一个和 Dubbo 类似的优化:
 * 当前服务自己就提供了该接口实现时，不再注册远程代理，直接走本地 Bean。
 *
 * @author Lion Li
 */
public class RemoteHttpServiceRegistrar extends AbstractHttpServiceRegistrar
    implements EnvironmentAware, ResourceLoaderAware, BeanClassLoaderAware {

    private Environment environment;
    private ResourceLoader resourceLoader;
    private ClassLoader beanClassLoader;
    private static final String SCAN_PACKAGES_PROPERTY = "remote.http.scan-packages";
    private static final AntPathMatcher PACKAGE_MATCHER = new AntPathMatcher(".");

    @Override
    public void setEnvironment(Environment environment) {
        super.setEnvironment(environment);
        this.environment = environment;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        super.setResourceLoader(resourceLoader);
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void setBeanClassLoader(ClassLoader beanClassLoader) {
        super.setBeanClassLoader(beanClassLoader);
        this.beanClassLoader = beanClassLoader;
    }

    @Override
    protected void registerHttpServices(GroupRegistry registry, AnnotationMetadata importingClassMetadata) {
        Set<String> scanPackagePatterns = new LinkedHashSet<>(resolveConfiguredScanPackages());
        if (scanPackagePatterns.isEmpty()) {
            return;
        }
        Set<String> scanBasePackages = resolveScanBasePackages(scanPackagePatterns);
        if (scanBasePackages.isEmpty()) {
            return;
        }
        // 先找出当前服务自己已经提供的远程接口，后面这些接口不再注册 HTTP client。
        Set<String> localServiceTypes = resolveLocalServiceTypes(scanBasePackages, scanPackagePatterns);
        MultiValueMap<String, String> groupedServices = resolveRemoteHttpServices(scanBasePackages, scanPackagePatterns, localServiceTypes);
        groupedServices.forEach((serviceId, classNames) ->
            registry.forGroup(serviceId).registerTypeNames(classNames.toArray(String[]::new)));
    }

    private MultiValueMap<String, String> resolveRemoteHttpServices(Set<String> basePackages, Set<String> scanPackagePatterns,
        Set<String> localServiceTypes) {
        MultiValueMap<String, String> groupedServices = new LinkedMultiValueMap<>();
        for (AnnotatedBeanDefinition beanDefinition : scanCandidateComponents(basePackages, RemoteHttpService.class)) {
            AnnotationMetadata metadata = beanDefinition.getMetadata();
            if (!metadata.isInterface() || !hasHttpExchange(metadata)) {
                continue;
            }
            String serviceTypeName = metadata.getClassName();
            if (!matchesConfiguredPackage(serviceTypeName, scanPackagePatterns)) {
                continue;
            }
            // 同服务场景直接依赖本地 provider，不再生成 HTTP 代理。
            if (localServiceTypes.contains(serviceTypeName)) {
                continue;
            }
            groupedServices.add(resolveServiceId(metadata), serviceTypeName);
        }
        return groupedServices;
    }

    private Set<String> resolveLocalServiceTypes(Set<String> basePackages, Set<String> scanPackagePatterns) {
        MultiValueMap<String, String> localServiceTypes = new LinkedMultiValueMap<>();
        for (AnnotatedBeanDefinition beanDefinition : scanCandidateComponents(basePackages, RemoteServiceController.class)) {
            String className = beanDefinition.getMetadata().getClassName();
            Class<?> beanClass = ClassUtils.resolveClassName(className, this.beanClassLoader);
            for (Class<?> interfaceType : ClassUtils.getAllInterfacesForClass(beanClass, this.beanClassLoader)) {
                if (interfaceType.isAnnotationPresent(RemoteHttpService.class)
                    && matchesConfiguredPackage(interfaceType.getName(), scanPackagePatterns)) {
                    localServiceTypes.add(interfaceType.getName(), className);
                }
            }
        }
        // 同一个远程接口只允许一个本地 provider，否则本地短路目标不明确。
        localServiceTypes.forEach((serviceTypeName, providerClassNames) -> {
            if (providerClassNames.size() > 1) {
                throw new IllegalStateException("Multiple local RemoteServiceController beans found for "
                    + serviceTypeName + ": " + providerClassNames);
            }
        });
        return new LinkedHashSet<>(localServiceTypes.keySet());
    }

    private List<AnnotatedBeanDefinition> scanCandidateComponents(Set<String> basePackages,
        Class<? extends Annotation> annotationType) {
        ClassPathScanningCandidateComponentProvider scanner = createScanner(annotationType);
        List<AnnotatedBeanDefinition> beanDefinitions = new ArrayList<>();
        for (String basePackage : basePackages) {
            for (BeanDefinition beanDefinition : scanner.findCandidateComponents(basePackage)) {
                if (beanDefinition instanceof AnnotatedBeanDefinition annotatedBeanDefinition) {
                    beanDefinitions.add(annotatedBeanDefinition);
                }
            }
        }
        return beanDefinitions;
    }

    private ClassPathScanningCandidateComponentProvider createScanner(Class<? extends Annotation> annotationType) {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false) {
            @Override
            protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
                return beanDefinition.getMetadata().isIndependent();
            }
        };
        scanner.addIncludeFilter(new AnnotationTypeFilter(annotationType));
        if (this.environment != null) {
            scanner.setEnvironment(this.environment);
        }
        if (this.resourceLoader != null) {
            scanner.setResourceLoader(this.resourceLoader);
        }
        return scanner;
    }

    private String resolveServiceId(AnnotationMetadata metadata) {
        Map<String, Object> attributes = metadata.getAnnotationAttributes(RemoteHttpService.class.getName());
        String serviceId = attributes != null ? String.valueOf(attributes.get("serviceId")) : StringUtils.EMPTY;
        if (StringUtils.isBlank(serviceId)) {
            throw new IllegalStateException("RemoteHttpService serviceId must not be blank: " + metadata.getClassName());
        }
        return serviceId;
    }

    private boolean hasHttpExchange(AnnotationMetadata metadata) {
        return metadata.isAnnotated(HttpExchange.class.getName()) || metadata.hasAnnotatedMethods(HttpExchange.class.getName());
    }

    private List<String> resolveConfiguredScanPackages() {
        if (this.environment == null) {
            return Collections.emptyList();
        }
        return Binder.get(this.environment).bind(SCAN_PACKAGES_PROPERTY, org.springframework.boot.context.properties.bind.Bindable.listOf(String.class))
            .orElseGet(Collections::emptyList)
            .stream()
            .filter(StringUtils::isNotBlank)
            .distinct()
            .toList();
    }

    private Set<String> resolveScanBasePackages(Set<String> scanPackagePatterns) {
        Set<String> basePackages = new LinkedHashSet<>();
        for (String packagePattern : scanPackagePatterns) {
            String basePackage = resolveScanBasePackage(packagePattern);
            if (StringUtils.isNotBlank(basePackage)) {
                basePackages.add(basePackage);
            }
        }
        return basePackages;
    }

    private String resolveScanBasePackage(String packagePattern) {
        int wildcardIndex = packagePattern.indexOf('*');
        if (wildcardIndex < 0) {
            return packagePattern;
        }
        String packagePrefix = packagePattern.substring(0, wildcardIndex);
        packagePrefix = StringUtils.substringBeforeLast(packagePrefix, ".");
        return StringUtils.defaultString(packagePrefix);
    }

    private boolean matchesConfiguredPackage(String className, Set<String> scanPackagePatterns) {
        if (scanPackagePatterns.isEmpty()) {
            return true;
        }
        String packageName = ClassUtils.getPackageName(className);
        for (String packagePattern : scanPackagePatterns) {
            if (PACKAGE_MATCHER.match(packagePattern, packageName)) {
                return true;
            }
        }
        return false;
    }
}
