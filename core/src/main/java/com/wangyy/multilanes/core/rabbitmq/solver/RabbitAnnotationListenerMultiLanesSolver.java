package com.wangyy.multilanes.core.rabbitmq.solver;

import com.wangyy.multilanes.core.trace.FeatureTagContext;
import com.wangyy.multilanes.core.utils.FeatureTagUtils;
import com.wangyy.multilanes.core.utils.ReflectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/*
 * Rabbit Listener监听队列需要打上featureTag
 *
 * 声明listener的场景有：
 * 1）声明MessageListenerContainer，需要修改MessageListenerContainer中的queues Name {@link RabbitListenerContainerMultiLanesIni}
 *
 * 2）@RabbitListener，需要修改注解中的QueueName {@link RabbitAnnotationListenerMultiLanesIni}
 * 3) 自定义annotation等等，需要增加相应打tag逻辑
 *
 */
@Slf4j
public class RabbitAnnotationListenerMultiLanesSolver {

    private BeanDefinitionRegistry registry;

    public RabbitAnnotationListenerMultiLanesSolver(BeanDefinitionRegistry registry) {
        this.registry = registry;
    }

    public void lance() {
        if (!FeatureTagUtils.needTag()) {
            log.info("base-lane listener need not to mock");
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
}
