package com.wangyy.multilanes.core.kafka;

import com.wangyy.multilanes.core.annotation.ConditionalOnConfig;
import com.wangyy.multilanes.core.kafka.consumer.KafkaConsumerTopicChangeProcessor;
import com.wangyy.multilanes.core.kafka.producer.KafkaProducerFeatureTagProcessor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.PriorityOrdered;
import org.springframework.stereotype.Component;

/**
 * Created by houyantao on 2023/1/31
 */
@ConditionalOnConfig("multi-lanes.enable")
@Component
public class KafkaMultiLanesBeanDefinitionIni implements BeanFactoryPostProcessor, PriorityOrdered {

    @Override
    public int getOrder() {
        return PriorityOrdered.HIGHEST_PRECEDENCE;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        new KafkaProducerFeatureTagProcessor(beanFactory).lance();
        new KafkaConsumerTopicChangeProcessor(beanFactory).lance();
    }
}
