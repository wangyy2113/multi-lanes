package com.wangyy.multilanes.core.rabbitmq.solver;

import com.wangyy.multilanes.core.trace.FeatureTagContext;
import com.wangyy.multilanes.core.utils.FeatureTagUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import java.util.Map;

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
public class RabbitListenerContainerMultiLanesSolver {

    private ConfigurableListableBeanFactory beanFactory;

    public RabbitListenerContainerMultiLanesSolver(ConfigurableListableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    public void lance() {
        if (!FeatureTagUtils.needTag()) {
            log.info("base-lane listenerContainer need not to mock");
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

            log.info("[multi-lanes] RabbitMQ mock MessageListenerContainer {} queueNames:{}", lc);
        });
    }
}
