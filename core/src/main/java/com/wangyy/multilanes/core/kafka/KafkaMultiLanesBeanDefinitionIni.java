package com.wangyy.multilanes.core.kafka;

import com.wangyy.multilanes.core.annotation.ConditionalOnConfig;
import com.wangyy.multilanes.core.kafka.consumer.KafkaAnnotationTopicChangeProcessor;
import com.wangyy.multilanes.core.kafka.producer.KafkaProducerFeatureTagProcessor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.core.PriorityOrdered;
import org.springframework.stereotype.Component;

/**
 * Created by houyantao on 2023/2/2
 */
@ConditionalOnConfig("multi-lanes.enable")
@Component
public class KafkaMultiLanesBeanDefinitionIni implements BeanDefinitionRegistryPostProcessor, PriorityOrdered {

    @Override
    public int getOrder() {
        return PriorityOrdered.HIGHEST_PRECEDENCE;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        new KafkaAnnotationTopicChangeProcessor(registry).lance();
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }
}
