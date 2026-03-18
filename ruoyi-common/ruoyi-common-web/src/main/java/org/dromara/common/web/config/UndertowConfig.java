//package org.dromara.common.web.config;
//
//import io.undertow.server.DefaultByteBufferPool;
//import io.undertow.server.handlers.DisallowedMethodsHandler;
//import io.undertow.util.HttpString;
//import io.undertow.websockets.jsr.WebSocketDeploymentInfo;
//import org.dromara.common.core.utils.SpringUtils;
//import org.springframework.boot.autoconfigure.AutoConfiguration;
//import org.springframework.boot.web.embedded.undertow.UndertowServletWebServerFactory;
//import org.springframework.boot.web.server.WebServerFactoryCustomizer;
//import org.springframework.core.task.VirtualThreadTaskExecutor;
//
///**
// * Boot 4 迁移后默认改用 Jetty，这里暂时保留 Undertow 配置实现以便后续回切时参考。
// */
//@AutoConfiguration
//public class UndertowConfig implements WebServerFactoryCustomizer<UndertowServletWebServerFactory> {
//
//    @Override
//    public void customize(UndertowServletWebServerFactory factory) {
//        factory.addDeploymentInfoCustomizers(deploymentInfo -> {
//            WebSocketDeploymentInfo webSocketDeploymentInfo = new WebSocketDeploymentInfo();
//            webSocketDeploymentInfo.setBuffers(new DefaultByteBufferPool(true, 1024));
//            deploymentInfo.addServletContextAttribute("io.undertow.websockets.jsr.WebSocketDeploymentInfo", webSocketDeploymentInfo);
//
//            if (SpringUtils.isVirtual()) {
//                VirtualThreadTaskExecutor executor = new VirtualThreadTaskExecutor("undertow-");
//                deploymentInfo.setExecutor(executor);
//                deploymentInfo.setAsyncExecutor(executor);
//            }
//
//            deploymentInfo.addInitialHandlerChainWrapper(handler -> {
//                HttpString[] disallowedHttpMethods = {
//                    HttpString.tryFromString("CONNECT"),
//                    HttpString.tryFromString("TRACE"),
//                    HttpString.tryFromString("TRACK")
//                };
//                return new DisallowedMethodsHandler(handler, disallowedHttpMethods);
//            });
//        });
//    }
//
//}
