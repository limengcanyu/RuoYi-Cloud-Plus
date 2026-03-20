package org.dromara.common.translation.config;

import lombok.extern.slf4j.Slf4j;
import org.dromara.common.translation.core.handler.TranslationBeanSerializerModifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import tools.jackson.databind.ser.SerializerFactory;

/**
 * 翻译模块额外修改jackson配置
 *
 * @author Lion Li
 */
@Slf4j
@AutoConfiguration(before = JacksonAutoConfiguration.class)
public class TranslationJacksonConfig {

    @Bean
    public JsonMapperBuilderCustomizer translationInitCustomizer() {
        return builder -> {
            SerializerFactory serializerFactory = builder.serializerFactory();
            serializerFactory = serializerFactory.withSerializerModifier(new TranslationBeanSerializerModifier());
            builder.serializerFactory(serializerFactory);
        };
    }

}
