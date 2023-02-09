package com.wangyy.multilanes.core.kafka.consumer;

import com.wangyy.multilanes.core.trace.FeatureTagContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.AbstractMessageListenerContainer;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Created by houyantao on 2023/2/9
 */
@Slf4j
public class KafkaConsumerGroupChangeProcessor {


    private ApplicationContext applicationContext;

    public KafkaConsumerGroupChangeProcessor(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void lance() {
        if (!FeatureTagContext.isBaseLine()) {
            changeContainerConsumerGroup();
        }
    }

    private void changeContainerConsumerGroup() {
        Map<String, AbstractMessageListenerContainer> containerMap = applicationContext.getBeansOfType(AbstractMessageListenerContainer.class);
        containerMap.values().forEach(container -> {
            try {
                log.info("start change consumer group");
                Field field = AbstractMessageListenerContainer.class.getDeclaredField("consumerFactory");
                field.setAccessible(true);
                ConsumerFactory consumerFactory = (ConsumerFactory) field.get(container);
                Field configsField = ConsumerFactory.class.getDeclaredField("configs");
                configsField.setAccessible(true);
                Map<String, Object> configs = (Map<String, Object>) configsField.get(consumerFactory);
                String originGroup = (String) configs.get(ConsumerConfig.GROUP_ID_CONFIG);
                String newGroup = originGroup + "_" + FeatureTagContext.getDEFAULT();
                configs.put(ConsumerConfig.GROUP_ID_CONFIG, newGroup);
                log.info("new consumer group is {}", newGroup);
            } catch (Exception e) {
                log.error("change consumer group fail", e);
            }
        });
    }
}
