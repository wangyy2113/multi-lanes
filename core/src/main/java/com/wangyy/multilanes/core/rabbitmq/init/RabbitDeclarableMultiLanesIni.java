package com.wangyy.multilanes.core.rabbitmq.init;

import com.wangyy.multilanes.core.annotation.ConditionalOnConfig;
import com.wangyy.multilanes.core.trace.FTConstants;
import com.wangyy.multilanes.core.trace.FeatureTagContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/*
 * 影子Exchange, Queue, Binding生成
 *
 */
@ConditionalOnConfig("multi-lanes.rabbit.enable")
@Slf4j
@Component
public class RabbitDeclarableMultiLanesIni implements ApplicationContextAware {

    @Autowired
    private RabbitAdmin rabbitAdmin;

    private ApplicationContext applicationContext;

    @Bean
    @ConditionalOnMissingBean
    public RabbitAdmin rabbitAdmin(RabbitTemplate rabbitTemplate) {
        return new RabbitAdmin(rabbitTemplate);
    }


    @PostConstruct
    public void rabbitExchangeIni() {
        String featureTag = FeatureTagContext.getDEFAULT();
        boolean needMock = !featureTag.equals(FTConstants.FEATURE_TAG_BASE_LANE_VALUE);
        if (!needMock) {
            log.info("main-lane need not to mock");
            return;
        }

        mockExchangeQueueBinding(featureTag);

        log.info("[multi-lanes] RabbitMQ mock finish");
    }

    private void mockExchangeQueueBinding(String featureTag) {
        applicationContext.getBeansOfType(RabbitDeclarableMultiLanesIni.class);
        List<Exchange> contextExchanges = new LinkedList(this.applicationContext.getBeansOfType(Exchange.class).values());
        List<Queue> contextQueues = new LinkedList(this.applicationContext.getBeansOfType(Queue.class).values());
        List<Binding> contextBindings = new LinkedList(this.applicationContext.getBeansOfType(Binding.class).values());
        processDeclarables(contextExchanges, contextQueues, contextBindings);

        contextExchanges.stream()
                .filter(ce -> !ce.isInternal())
                .forEach(ce -> {
                    String mockExchangeName = buildWithFeatureTag(ce.getName(), featureTag);
                    Exchange mockExchange = null;
                    switch (ce.getType()) {
                        case "topic":
                            mockExchange = new TopicExchange(mockExchangeName, false, true);
                            break;
                        case "direct":
                            mockExchange = new DirectExchange(mockExchangeName, false, true);
                            break;
                        case "headers":
                            mockExchange = new HeadersExchange(mockExchangeName, false, true);
                            break;
                        case "fanout":
                            mockExchange = new FanoutExchange(mockExchangeName, false, true);
                            break;
                        default:
                            throw new IllegalArgumentException("RabbitExchange Type is not supported for now : " + ce.getType());
                    }
                    if (mockExchange != null) {
                        rabbitAdmin.declareExchange(mockExchange);
                        log.info("[multi-lanes] RabbitMQ mock Exchange:{}", mockExchange);
                    }
                });


        contextQueues.forEach(cq -> {
            String mockQueueName = buildWithFeatureTag(cq.getName(), featureTag);
            Queue mockQueue = new Queue(mockQueueName, false, false, true);
            rabbitAdmin.declareQueue(mockQueue);
            log.info("[multi-lanes] RabbitMQ mock Queue:{}", mockQueue);
        });

        contextBindings.forEach(cb -> {
            String desti = buildWithFeatureTag(cb.getDestination(), featureTag);
            String exchange = buildWithFeatureTag(cb.getExchange(), featureTag);
            Binding mockBinding = new Binding(desti, cb.getDestinationType(), exchange, cb.getRoutingKey(), cb.getArguments());
            rabbitAdmin.declareBinding(mockBinding);
            log.info("[multi-lanes] RabbitMQ mock Binding:{}", mockBinding);
        });
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

    private String buildWithFeatureTag(String str, String featureTag) {
        return str + "_" + featureTag;
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
