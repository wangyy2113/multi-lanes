package com.wangyy.multilanes.core.kafka.consumer;

import com.wangyy.multilanes.core.kafka.KafkaNodeWatcher;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by houyantao on 2023/1/31
 */
@Slf4j
public class KafkaConsumerTopicRegisterProcessor {

    private ApplicationContext applicationContext;

    private KafkaNodeWatcher nodeWatcher;

    public KafkaConsumerTopicRegisterProcessor(ApplicationContext applicationContext, KafkaNodeWatcher nodeWatcher) {
        this.applicationContext = applicationContext;
        this.nodeWatcher = nodeWatcher;
    }

    public void registerToZookeeper() {
        registerContainerTopicGroupPath();
        registerAnnotationTopicGroupPath();
    }

    private void registerContainerTopicGroupPath() {
        Map<String, KafkaMessageListenerContainer> containerMap = applicationContext.getBeansOfType(KafkaMessageListenerContainer.class);
        for (KafkaMessageListenerContainer container : containerMap.values()) {
            String groupId = container.getGroupId();
            String[] topics = container.getContainerProperties().getTopics();
            for (String topic : topics) {
                nodeWatcher.registerZKPath(KafkaNodeWatcher.path(topic, groupId));
            }
        }
    }

    private void registerAnnotationTopicGroupPath() {
        List<Method> kafkaListenerMethods = findKafkaListenerMethods();
        if (CollectionUtils.isEmpty(kafkaListenerMethods)) {
            return;
        }
        log.info("start register annotation consumer zk");
        for (Method method : kafkaListenerMethods) {
            KafkaListener kafkaListener = method.getAnnotation(KafkaListener.class);
            String[] topics = kafkaListener.topics();
            String groupId = findKafkaListenerGroupId(kafkaListener);
            for (String topic : topics) {
                nodeWatcher.registerZKPath(KafkaNodeWatcher.path(topic, groupId));
            }
        }
    }

    private List<Method> findKafkaListenerMethods() {
        List<Method> methods = new ArrayList<>();
        String[] beanNames = applicationContext.getBeanNamesForAnnotation(KafkaListener.class);
        for (String beanName : beanNames) {
            Object bean = applicationContext.getBean(beanName);
            Class<?> beanClass = bean.getClass();
            ReflectionUtils.doWithMethods(beanClass, methods::add);
        }
        return methods.stream().filter(method -> method.isAnnotationPresent(KafkaListener.class)).collect(Collectors.toList());
    }

    private String findKafkaListenerGroupId(KafkaListener kafkaListener) {
        if (!StringUtils.isEmpty(kafkaListener.groupId())) {
            return kafkaListener.groupId();
        }
        ConcurrentKafkaListenerContainerFactory containerFactory = (ConcurrentKafkaListenerContainerFactory) applicationContext.getBean(kafkaListener.containerFactory());
        return (String) containerFactory.getConsumerFactory().getConfigurationProperties().get(ConsumerConfig.GROUP_ID_CONFIG);
    }
}
