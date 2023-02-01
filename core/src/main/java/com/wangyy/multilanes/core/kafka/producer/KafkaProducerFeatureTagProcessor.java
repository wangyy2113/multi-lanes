package com.wangyy.multilanes.core.kafka.producer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.internals.ProducerInterceptors;
import org.springframework.context.ApplicationContext;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * Created by houyantao on 2023/1/31
 */
@Slf4j
public class KafkaProducerFeatureTagProcessor {

    private ApplicationContext applicationContext;

    public KafkaProducerFeatureTagProcessor(ApplicationContext beanFactory) {
        this.applicationContext = beanFactory;
    }

    public void lance() {
        addInterceptorToKafkaProducerFactory();
        addInterceptorToKafkaProducer();
    }

    private void addInterceptorToKafkaProducerFactory() {
        Map<String, DefaultKafkaProducerFactory> producerFactoryMap = applicationContext.getBeansOfType(DefaultKafkaProducerFactory.class);
        producerFactoryMap.values().forEach(factory -> {
            try {
                Field field = DefaultKafkaProducerFactory.class.getDeclaredField("configs");
                field.setAccessible(true);
                Map<String, Object> props = (Map<String, Object>) field.get(factory);
                String interceptorClasses = props.get(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG) == null ?
                        MultiLanesProducerInterceptor.class.getName() :
                        props.get(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG) + "," + MultiLanesProducerInterceptor.class.getName();
                props.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, interceptorClasses);
            } catch (Exception e) {
                log.error("add interceptor to kafka producer factory fail", e);
            }
        });
    }

    private void addInterceptorToKafkaProducer() {
        Map<String, KafkaProducer> producerMap = applicationContext.getBeansOfType(KafkaProducer.class);
        producerMap.values().forEach(producer -> {
            try {
                Field interceptorsField = KafkaProducer.class.getDeclaredField("interceptors");
                interceptorsField.setAccessible(true);
                ProducerInterceptors producerInterceptors = (ProducerInterceptors) interceptorsField.get(producer);

                Field proInterceptorsField = ProducerInterceptors.class.getDeclaredField("interceptors");
                proInterceptorsField.setAccessible(true);
                List<ProducerInterceptor> interceptors = (List<ProducerInterceptor>) proInterceptorsField.get(producerInterceptors);
                interceptors.add(MultiLanesProducerInterceptor.instance());
            } catch (Exception e) {
                log.error("add interceptor to kafka producer fail", e);
            }
        });
    }
}
