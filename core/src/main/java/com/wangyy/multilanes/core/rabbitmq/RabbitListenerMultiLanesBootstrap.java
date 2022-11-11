package com.wangyy.multilanes.core.rabbitmq;

import com.wangyy.multilanes.core.annotation.ConditionalOnConfig;
import com.wangyy.multilanes.core.trace.FeatureTagContext;
import com.wangyy.multilanes.core.utils.FeatureTagUtils;
import com.wangyy.multilanes.core.utils.ReflectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer;
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
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/*
 * Rabbit Listener监听队列需要打上featureTag
 *
 * 声明listener的场景有：
 * 1）声明MessageListenerContainer，需要修改MessageListenerContainer中的queues Name
 * 2）@RabbitListener，需要修改注解中的QueueName
 * 3) 自定义annotation等等，需要增加相应打tag逻辑
 *
 */
@ConditionalOnConfig("multi-lanes.rabbit.enable")
@Slf4j
@Component
public class RabbitListenerMultiLanesBootstrap implements BeanDefinitionRegistryPostProcessor, PriorityOrdered {

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        if (!FeatureTagUtils.needTag()) {
            log.info("main-lane listener need not to mock");
            return;
        }
        String featureTag = FeatureTagContext.getDEFAULT();
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

            annotationMock(featureTag, clazz);
        }
    }

    private void annotationMock(String featureTag, Class clazz) {
        rabbitListenerMod(featureTag, clazz);
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
                .map(q -> FeatureTagUtils.buildWithFeatureTag(q, featureTag))
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
        if (!FeatureTagUtils.needTag()) {
            log.info("main-lane listenerContainer need not to mock");
            return;
        }
        mockMessageListenerContainer(beanFactory);
    }

    private void mockMessageListenerContainer(ConfigurableListableBeanFactory beanFactory) {
        Map<String, AbstractMessageListenerContainer> listenerContainerMap = beanFactory.getBeansOfType(AbstractMessageListenerContainer.class);
        if (listenerContainerMap.isEmpty()) {
            return;
        }
        String featureTag = FeatureTagContext.getDEFAULT();
        listenerContainerMap.values().forEach(lc -> {
            String[] queueNames = lc.getQueueNames();
            for (int i = 0; i < queueNames.length; i++) {
                String qn = queueNames[i];
                if (!qn.endsWith(featureTag)) {
                    queueNames[i] = FeatureTagUtils.buildWithFeatureTag(qn, featureTag);
                }
            }
            lc.setQueueNames(queueNames);
        });
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
