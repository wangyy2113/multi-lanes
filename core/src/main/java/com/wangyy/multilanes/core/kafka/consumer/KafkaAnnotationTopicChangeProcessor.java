package com.wangyy.multilanes.core.kafka.consumer;

import com.wangyy.multilanes.core.trace.FeatureTagContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by houyantao on 2023/2/2
 */
@Slf4j
public class KafkaAnnotationTopicChangeProcessor {

    private BeanDefinitionRegistry registry;

    public KafkaAnnotationTopicChangeProcessor(BeanDefinitionRegistry registry) {
        this.registry = registry;
    }

    public void lance() {
        if (!FeatureTagContext.isBaseLine()) {
            changeListenerAnnotationTopic();
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

    private List<Method> findKafkaListenerMethods() {
        List<Method> methods = new ArrayList<>();
        for (String beanName : registry.getBeanDefinitionNames()) {
            BeanDefinition definition = registry.getBeanDefinition(beanName);
            if (definition.getBeanClassName() == null) {
                continue;
            }
            Class clazz;
            try {
                clazz = Class.forName(definition.getBeanClassName());
            } catch (Exception e) {
                log.error("invalid class name: " + definition.getBeanClassName(), e);
                continue;
            }
            ReflectionUtils.doWithMethods(clazz, methods::add, method -> method.isAnnotationPresent(KafkaListener.class));
        }
        return methods;
    }
}
