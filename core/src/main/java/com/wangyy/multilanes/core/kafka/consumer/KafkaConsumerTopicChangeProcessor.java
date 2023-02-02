package com.wangyy.multilanes.core.kafka.consumer;

import com.wangyy.multilanes.core.trace.FeatureTagContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;

import java.lang.reflect.Field;
import java.util.Map;

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
            changeContainerTopic();
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

}
