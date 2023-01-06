package com.wangyy.multilanes.core.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 更改消费者监听的 topic，增加 Interceptor
 */
@Slf4j
@Component
public class KafkaConsumerBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        changeListenerAnnotationTopic(bean);
        changeContainerTopic(bean);
        addInterceptorToContainer(bean);
        addInterceptorToContainerFactory(bean);
        return bean;
    }

    private void changeListenerAnnotationTopic(Object bean) {
        List<Method> methods = new ArrayList<>();
        ReflectionUtils.doWithMethods(bean.getClass(), methods::add);
        List<Method> kafkaListenerMethods = methods.stream().filter(method -> method.isAnnotationPresent(KafkaListener.class)).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(kafkaListenerMethods)) {
            return;
        }
        log.info("start change annotation consumer topic");
        for (Method method : kafkaListenerMethods) {
            KafkaListener kafkaListener = method.getAnnotation(KafkaListener.class);
            String[] originTopic = kafkaListener.topics();
            String[] newTopics = new String[originTopic.length];
            for (int i = 0; i < originTopic.length; i++) {
                newTopics[i] = originTopic[i] + "-back";
            }
            com.wangyy.multilanes.core.utils.ReflectionUtils.changeAnnotationValue(kafkaListener, "topics", newTopics);
            log.info("after change, kafka listener topics is {}", kafkaListener.topics());
        }
    }

    private void changeContainerTopic(Object bean) {
        if (!(bean instanceof KafkaMessageListenerContainer)) {
            return;
        }
        try {
            KafkaMessageListenerContainer kafkaMessageListenerContainer = (KafkaMessageListenerContainer) bean;
            Field field = KafkaMessageListenerContainer.class.getSuperclass().getDeclaredField("containerProperties");
            field.setAccessible(true);  // 设置可访问性
            ContainerProperties containerProperties = (ContainerProperties) field.get(kafkaMessageListenerContainer);  // 获取 consumerProperties 对象
            Field topicField = ContainerProperties.class.getSuperclass().getDeclaredField("topics");
            topicField.setAccessible(true);  // 设置可访问性
            String[] originTopic = (String[]) topicField.get(containerProperties);
            String[] newTopics = new String[originTopic.length];
            for (int i = 0; i < originTopic.length; i++) {
                newTopics[i] = originTopic[i] + "-back";
            }
            topicField.set(containerProperties, newTopics);  // 修改 topics 字段的值
        } catch (Exception e) {
            log.error("change kafka consumer container topic error", e);
        }
    }

    private void addInterceptorToContainer(Object bean) {
        if (!(bean instanceof KafkaMessageListenerContainer)) {
            return;
        }
        KafkaMessageListenerContainer kafkaMessageListenerContainer = (KafkaMessageListenerContainer) bean;
        kafkaMessageListenerContainer.setRecordInterceptor(MultiLanesConsumerInterceptor.instance());
    }

    private void addInterceptorToContainerFactory(Object bean) {
        if (!(bean instanceof ConcurrentKafkaListenerContainerFactory)) {
            return;
        }
        ConcurrentKafkaListenerContainerFactory containerFactory = (ConcurrentKafkaListenerContainerFactory) bean;
        containerFactory.setRecordInterceptor(MultiLanesConsumerInterceptor.instance());
    }

}
