package com.wangyy.multilanes.core.rabbitmq.init;

import com.wangyy.multilanes.core.trace.FTConstants;
import com.wangyy.multilanes.core.trace.FeatureTagContext;
import com.wangyy.multilanes.core.utils.ReflectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static com.wangyy.multilanes.core.trace.FTConstants.buildWithFeatureTag;

/*
 * 将Exchange, Queue, Binding修改为带featureTag
 *
 *
 */
@Slf4j
public class RabbitDeclarableMultiLanesIni {

    private ApplicationContext applicationContext;

    public RabbitDeclarableMultiLanesIni(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void init() throws Exception  {
        String featureTag = FeatureTagContext.getDEFAULT();
        boolean needMock = !featureTag.equals(FTConstants.FEATURE_TAG_BASE_LANE_VALUE);
        if (!needMock) {
            log.info("main-lane need not to mock");
            return;
        }

        mockExchangeQueueBinding(featureTag);

        log.info("[multi-lanes] RabbitMQ mock finished");
    }

    private void mockExchangeQueueBinding(String featureTag) throws Exception  {
        applicationContext.getBeansOfType(RabbitDeclarableMultiLanesIni.class);
        List<Exchange> contextExchanges = new LinkedList(this.applicationContext.getBeansOfType(Exchange.class).values());
        List<Queue> contextQueues = new LinkedList(this.applicationContext.getBeansOfType(Queue.class).values());
        List<Binding> contextBindings = new LinkedList(this.applicationContext.getBeansOfType(Binding.class).values());
        processDeclarables(contextExchanges, contextQueues, contextBindings);

        for (Exchange ce : contextExchanges) {
            if (ce.isInternal()) {
                continue;
            }
            if (ce.getName().endsWith(featureTag)) {
                log.info("[multi-lanes] RabbitMQ exchange mock SKIP. {}", ce.getName());
                continue;
            }
            String mockExchangeName = buildWithFeatureTag(ce.getName(), featureTag);
            Field nameFiled = AbstractExchange.class.getDeclaredField("name");
            ReflectionUtils.setField(nameFiled, mockExchangeName, ce);
            log.info("[multi-lanes] RabbitMQ mock Exchange:{}", ce);
        }

        for (Queue cq : contextQueues) {
            if (cq.getName().endsWith(featureTag)) {
                log.info("[multi-lanes] RabbitMQ queue mock SKIP. {}", cq.getName());
                continue;
            }
            String mockQueueName = buildWithFeatureTag(cq.getName(), featureTag);
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
            String mockDestination = buildWithFeatureTag(cb.getDestination(), featureTag);
            String mockExchange = buildWithFeatureTag(cb.getExchange(), featureTag);

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
    private void processDeclarables(Collection<Exchange> contextExchanges, Collection<Queue> contextQueues, Collection<Binding> contextBindings) {
        Collection<Declarables> declarables = this.applicationContext.getBeansOfType(Declarables.class, false, true).values();
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
