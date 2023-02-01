package com.wangyy.multilanes.core.kafka.consumer;

import com.wangyy.multilanes.core.trace.FeatureTagContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by houyantao on 2023/1/31
 */
@Slf4j
public class KafkaConsumerTopicChangeProcessor {

    private ApplicationContext applicationContext;

    public KafkaConsumerTopicChangeProcessor(ApplicationContext beanFactory) {
        this.applicationContext = beanFactory;
    }

    public void lance() {
        if (!FeatureTagContext.isBaseLine()) {
            changeListenerAnnotationTopic();
            changeContainerTopic();
        }
    }

    private void changeListenerAnnotationTopic() {
        List<Method> kafkaListenerMethods = findKafkaListenerMethods();
        if (CollectionUtils.isEmpty(kafkaListenerMethods)) {
            return;
        }
        log.info("start change annotation consumer topic");
        for (Method method : kafkaListenerMethods) {
            KafkaListener kafkaListener = method.getAnnotation(KafkaListener.class);
            String[] originTopic = kafkaListener.topics();
            String[] newTopics = new String[originTopic.length];
            for (int i = 0; i < originTopic.length; i++) {
                newTopics[i] = originTopic[i] + "_" + FeatureTagContext.getDEFAULT();
            }
            com.wangyy.multilanes.core.utils.ReflectionUtils.changeAnnotationValue(kafkaListener, "topics", newTopics);
            log.info("after change, kafka listener topics is {}", kafkaListener.topics());
        }
    }

    private void changeContainerTopic() {
        Map<String, KafkaMessageListenerContainer> containerMap = applicationContext.getBeansOfType(KafkaMessageListenerContainer.class);
        containerMap.values().forEach(container -> {
            try {
                Field field = KafkaMessageListenerContainer.class.getSuperclass().getDeclaredField("containerProperties");
                field.setAccessible(true);
                ContainerProperties containerProperties = (ContainerProperties) field.get(container);  // 获取 consumerProperties 对象
                Field topicField = ContainerProperties.class.getSuperclass().getDeclaredField("topics");
                topicField.setAccessible(true);
                String[] originTopic = (String[]) topicField.get(containerProperties);
                String[] newTopics = new String[originTopic.length];
                for (int i = 0; i < originTopic.length; i++) {
                    newTopics[i] = originTopic[i] + "_" + FeatureTagContext.getDEFAULT();
                }
                topicField.set(containerProperties, newTopics); // 修改 topics 字段的值
            } catch (Exception e) {
                log.error("change kafka consumer container topic error", e);
            }
        });
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
}
