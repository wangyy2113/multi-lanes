package com.wangyy.multilanes.core.rabbitmq.solver;

import com.wangyy.multilanes.core.trace.FeatureTagContext;
import com.wangyy.multilanes.core.utils.FeatureTagUtils;
import com.wangyy.multilanes.core.utils.ReflectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/*
 * 将Exchange, Queue, Binding修改为带featureTag
 *
 */
@Slf4j
public class RabbitDeclarableMultiLanesSolver {

    private ConfigurableListableBeanFactory beanFactory;

    public RabbitDeclarableMultiLanesSolver(ConfigurableListableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    public void lance() {
        if (!FeatureTagUtils.needTag()) {
            log.info("main-lane need not to mock");
            return;
        }
        String featureTag = FeatureTagContext.getDEFAULT();
        try {
            mockExchangeQueueBinding(beanFactory, featureTag);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        log.info("[multi-lanes] RabbitMQ mock finished");
    }

    private void mockExchangeQueueBinding(ConfigurableListableBeanFactory beanFactory, String featureTag) throws Exception  {
        List<Exchange> contextExchanges = new LinkedList(beanFactory.getBeansOfType(Exchange.class).values());
        List<Queue> contextQueues = new LinkedList(beanFactory.getBeansOfType(Queue.class).values());
        List<Binding> contextBindings = new LinkedList(beanFactory.getBeansOfType(Binding.class).values());
        processDeclarables(beanFactory, contextExchanges, contextQueues, contextBindings);

        for (Exchange ce : contextExchanges) {
            if (ce.isInternal()) {
                continue;
            }
            if (ce.getName().endsWith(featureTag)) {
                log.info("[multi-lanes] RabbitMQ exchange mock SKIP. {}", ce.getName());
                continue;
            }
            String mockExchangeName = FeatureTagUtils.buildWithFeatureTag(ce.getName(), featureTag);
            Field nameFiled = AbstractExchange.class.getDeclaredField("name");
            ReflectionUtils.setField(nameFiled, mockExchangeName, ce);
            log.info("[multi-lanes] RabbitMQ mock Exchange:{}", ce);

            //TODO add mockExchangeName into Redis
        }

        for (Queue cq : contextQueues) {
            if (cq.getName().endsWith(featureTag)) {
                log.info("[multi-lanes] RabbitMQ queue mock SKIP. {}", cq.getName());
                continue;
            }
            String mockQueueName = FeatureTagUtils.buildWithFeatureTag(cq.getName(), featureTag);
            Field nameFiled = Queue.class.getDeclaredField("name");
            Field actualNameFiled = Queue.class.getDeclaredField("actualName");
            ReflectionUtils.setField(nameFiled, mockQueueName, cq);
            ReflectionUtils.setField(actualNameFiled, mockQueueName, cq);
            log.info("[multi-lanes] RabbitMQ mock Queue:{}", cq);
        }

        for (Binding cb : contextBindings) {
            if (cb.getExchange().endsWith(featureTag)) {
                log.info("[multi-lanes] RabbitMQ binding mock SKIP. {}", cb.getExchange());
                continue;
            }
            String mockDestination = FeatureTagUtils.buildWithFeatureTag(cb.getDestination(), featureTag);
            String mockExchange = FeatureTagUtils.buildWithFeatureTag(cb.getExchange(), featureTag);

            Field destinationFiled = Binding.class.getDeclaredField("destination");
            Field exchangeFiled = Binding.class.getDeclaredField("exchange");
            ReflectionUtils.setField(destinationFiled, mockDestination, cb);
            ReflectionUtils.setField(exchangeFiled, mockExchange, cb);

            log.info("[multi-lanes] RabbitMQ mock Binding:{}", cb);
        }
    }

    /*
     * copy from {@link org.springframework.amqp.rabbit.core.RabbitAdmin#processDeclarables}
     *
     */
    private void processDeclarables(ConfigurableListableBeanFactory beanFactory, Collection<Exchange> contextExchanges, Collection<Queue> contextQueues, Collection<Binding> contextBindings) {
        Collection<Declarables> declarables = beanFactory.getBeansOfType(Declarables.class, false, true).values();
        declarables.forEach(d ->
                d.getDeclarables().forEach(declarable -> {
                    if (declarable instanceof Exchange) {
                        contextExchanges.add((Exchange) declarable);
                    } else if (declarable instanceof Queue) {
                        contextQueues.add((Queue) declarable);
                    } else if (declarable instanceof Binding) {
                        contextBindings.add((Binding) declarable);
                    }
                })
        );
    }
}
