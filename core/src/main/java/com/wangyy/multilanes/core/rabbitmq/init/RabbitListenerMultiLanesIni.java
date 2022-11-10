package com.wangyy.multilanes.core.rabbitmq.init;

import com.wangyy.multilanes.core.trace.FTConstants;
import com.wangyy.multilanes.core.trace.FeatureTagContext;
import com.wangyy.multilanes.core.utils.ReflectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/*
 * Rabbit Listener监听队列需要打上featureTag
 *
 * 很多场景是通过注解的方式声明listener，注解中会指定QueueName等字段，Multi-Lanes需要将QueueName加上featureTag
 * @RabbitListener
 *
 */
@Slf4j
public class RabbitListenerMultiLanesIni {

    private ApplicationContext applicationContext;

    public RabbitListenerMultiLanesIni(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void init() {
        String featureTag = FeatureTagContext.getDEFAULT();
        boolean needMock = !featureTag.equals(FTConstants.FEATURE_TAG_BASE_LANE_VALUE);
        if (!needMock) {
            log.info("main-lane listener need not to mock");
            return;
        }
        rabbitListenerInit(featureTag);
    }

    private void rabbitListenerInit(String featureTag) {
        Map<String, Object> controllers = applicationContext.getBeansWithAnnotation(RabbitListener.class);
        for (Map.Entry<String, Object> entry : controllers.entrySet()) {
            Object value = entry.getValue();

            Class<?> aClass = AopUtils.getTargetClass(value);
            RabbitListener annotation = aClass.getDeclaredAnnotation(RabbitListener.class);

            //TODO 暂时只改了queues这一项
            String[] queues = annotation.queues();
            if (queues.length == 0) {
                continue;
            }

            List<String> ql = Stream.of(queues)
                    .filter(q -> !q.endsWith(featureTag))
                    .map(q -> FTConstants.buildWithFeatureTag(q, featureTag))
                    .collect(Collectors.toList());

            if (CollectionUtils.isEmpty(ql)) {
                continue;
            }

            String[] queuesWithFeatureTag = ql.toArray(new String[ql.size()]);

            ReflectionUtils.changeAnnotationValue(annotation, "queues", queuesWithFeatureTag);
            log.info("[multi-lanes] RabbitMQ mock Listener class:{} Annotation:{} newQueues:{}", aClass.getName(), annotation, queuesWithFeatureTag);

        }
    }

}
