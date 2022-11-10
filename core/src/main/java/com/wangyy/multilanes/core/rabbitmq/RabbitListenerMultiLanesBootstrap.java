package com.wangyy.multilanes.core.rabbitmq;

import com.wangyy.multilanes.core.annotation.ConditionalOnConfig;
import com.wangyy.multilanes.core.trace.FTConstants;
import com.wangyy.multilanes.core.trace.FeatureTagContext;
import com.wangyy.multilanes.core.utils.ReflectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/*
 * Rabbit Listener监听队列需要打上featureTag
 *
 * 很多场景是通过注解的方式声明listener，注解中会指定QueueName等字段，Multi-Lanes需要将QueueName加上featureTag
 * @RabbitListener
 *
 */
@ConditionalOnConfig("multi-lanes.rabbit.enable")
@Slf4j
@Component
public class RabbitListenerMultiLanesBootstrap implements BeanDefinitionRegistryPostProcessor, PriorityOrdered {

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        String featureTag = FeatureTagContext.getDEFAULT();
        boolean needMock = !featureTag.equals(FTConstants.FEATURE_TAG_BASE_LANE_VALUE);
        if (!needMock) {
            log.info("main-lane listener need not to mock");
            return;
        }

        String[] beanNames = registry.getBeanDefinitionNames();

        for (String beanName : beanNames) {
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
            rabbitListenerMod(featureTag, clazz);
        }
    }

    private void rabbitListenerMod(String featureTag, Class clazz) {
        if (!clazz.isAnnotationPresent(RabbitListener.class)) {
            return;
        }
        RabbitListener annotation = (RabbitListener) clazz.getDeclaredAnnotation(RabbitListener.class);
        //TODO 暂时只改了queues这一项
        String[] queues = annotation.queues();
        if (queues.length == 0) {
            return;
        }

        List<String> ql = Stream.of(queues)
                .filter(q -> !q.endsWith(featureTag))
                .map(q -> FTConstants.buildWithFeatureTag(q, featureTag))
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(ql)) {
            return;
        }

        String[] queuesWithFeatureTag = ql.toArray(new String[ql.size()]);

        ReflectionUtils.changeAnnotationValue(annotation, "queues", queuesWithFeatureTag);
        log.info("[multi-lanes] RabbitMQ mock Listener class:{} Annotation:{} newQueues:{}", clazz.getName(), annotation, queuesWithFeatureTag);
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
