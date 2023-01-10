package com.wangyy.multilanes.core.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.internals.ProducerInterceptors;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * Created by houyantao on 2023/1/6
 */
@Slf4j
public class KafkaProducerBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        addInterceptorToKafkaProducer(bean);
        addInterceptorToKafkaProducerFactory(bean);
        return bean;
    }

    private void addInterceptorToKafkaProducerFactory(Object bean) {
        if (!(bean instanceof DefaultKafkaProducerFactory)) {
            return;
        }
        try {
            DefaultKafkaProducerFactory factory = (DefaultKafkaProducerFactory) bean;
            Field field = DefaultKafkaProducerFactory.class.getDeclaredField("configs");
            field.setAccessible(true);
            Map<String, Object> props = (Map<String, Object>) field.get(factory);
            props.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, MultiLanesProducerInterceptor.class.getName());
        } catch (Exception e) {
            log.info("add interceptor to kafka producer factory fail", e);
        }
    }

    private void addInterceptorToKafkaProducer(Object bean) {
        if (!(bean instanceof KafkaProducer)) {
            return;
        }
        try {
            KafkaProducer producer = (KafkaProducer) bean;
            Field interceptorsField = KafkaProducer.class.getDeclaredField("interceptors");
            interceptorsField.setAccessible(true);
            ProducerInterceptors producerInterceptors = (ProducerInterceptors) interceptorsField.get(producer);

            Field proInterceptorsField = ProducerInterceptors.class.getDeclaredField("interceptors");
            proInterceptorsField.setAccessible(true);
            List<ProducerInterceptor> interceptors = (List<ProducerInterceptor>) proInterceptorsField.get(producerInterceptors);
            interceptors.add(MultiLanesProducerInterceptor.instance());
        } catch (Exception e) {
            log.info("add interceptor to kafka producer fail", e);
        }
    }
}
