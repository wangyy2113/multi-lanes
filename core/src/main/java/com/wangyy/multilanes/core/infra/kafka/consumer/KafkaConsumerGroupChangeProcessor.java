package com.wangyy.multilanes.core.infra.kafka.consumer;

import com.wangyy.multilanes.core.trace.FeatureTagContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.kafka.config.AbstractKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.AbstractMessageListenerContainer;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Created by houyantao on 2023/2/9
 */
@Slf4j
public class KafkaConsumerGroupChangeProcessor {

    private final ApplicationContext applicationContext;

    public KafkaConsumerGroupChangeProcessor(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void lance() {
        if (!FeatureTagContext.isBaseLine()) {
            changeContainerConsumerGroup();
            changeContainerFactoryConsumerGroup();
        }
    }

    private void changeContainerConsumerGroup() {
        Map<String, AbstractMessageListenerContainer> containerMap = applicationContext.getBeansOfType(AbstractMessageListenerContainer.class);
        containerMap.values().forEach(container -> {
            try {
                log.info("start change container consumer group");
                Field field = AbstractMessageListenerContainer.class.getDeclaredField("consumerFactory");
                field.setAccessible(true);
                changeConsumerGroup(field, container);
            } catch (Exception e) {
                log.error("change consumer container group fail", e);
            }
        });
    }

    private void changeContainerFactoryConsumerGroup() {
        Map<String, AbstractKafkaListenerContainerFactory> factoryMap = applicationContext.getBeansOfType(AbstractKafkaListenerContainerFactory.class);
        factoryMap.values().forEach(factory -> {
            log.info("start change container factory consumer group");
            try {
                Field field = AbstractKafkaListenerContainerFactory.class.getDeclaredField("consumerFactory");
                field.setAccessible(true);
                changeConsumerGroup(field, factory);
            } catch (Exception e) {
                log.error("change container factory consumer group fail", e);
            }
        });
    }

    private void changeConsumerGroup(Field field, Object bean) throws NoSuchFieldException, IllegalAccessException {
        DefaultKafkaConsumerFactory consumerFactory = (DefaultKafkaConsumerFactory) field.get(bean);
        Field configsField = DefaultKafkaConsumerFactory.class.getDeclaredField("configs");
        configsField.setAccessible(true);
        Map<String, Object> configs = (Map<String, Object>) configsField.get(consumerFactory);
        String originGroup = (String) configs.get(ConsumerConfig.GROUP_ID_CONFIG);
        if (StringUtils.isEmpty(originGroup)) {
            log.info("this bean maybe registered by springboot autoconfigure rather than the project, we will skip it");
            return;
        }
        String newGroup = originGroup + "_" + FeatureTagContext.getDEFAULT();
        configs.put(ConsumerConfig.GROUP_ID_CONFIG, newGroup);
        log.info("new consumer group is {}", newGroup);
    }

}
