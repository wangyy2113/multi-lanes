package com.wangyy.multilanes.core.kafka;

import com.wangyy.multilanes.core.trace.FeatureTagContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 1. 更改监听的 topic
 * 2. 向 zk 注册路径
 * 3. 为消费端增加切面
 */
@Slf4j
@Deprecated
public class KafkaConsumerBeanPostProcessor implements BeanPostProcessor {

    private final KafkaNodeWatcher nodeWatcher;

    private final ApplicationContext applicationContext;

    private final MultiLanesConsumerInterceptor consumerInterceptor;

    public KafkaConsumerBeanPostProcessor(KafkaNodeWatcher nodeWatcher, ApplicationContext applicationContext, MultiLanesConsumerInterceptor consumerInterceptor) {
        this.nodeWatcher = nodeWatcher;
        this.applicationContext = applicationContext;
        this.consumerInterceptor = consumerInterceptor;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        //change topic
        if (!FeatureTagContext.isBaseLine()) {
            changeListenerAnnotationTopic(bean);
            changeContainerTopic(bean);
        }
        // add consumer interceptor
        addInterceptorToContainer(bean);
        addInterceptorToContainerFactory(bean);

        //register path to zk
        registerContainerTopicGroupPath(bean);
        registerAnnotationTopicGroupPath(bean);
        return bean;
    }

    private void changeListenerAnnotationTopic(Object bean) {
        List<Method> kafkaListenerMethods = findKafkaListenerMethods(bean);
        if (CollectionUtils.isEmpty(kafkaListenerMethods)) {
            return;
        }
        log.info("start change annotation consumer topic");
        for (Method method : kafkaListenerMethods) {
            KafkaListener kafkaListener = method.getAnnotation(KafkaListener.class);
            String[] originTopic = kafkaListener.topics();
            String[] newTopics = new String[originTopic.length];
            for (int i = 0; i < originTopic.length; i++) {
                newTopics[i] = originTopic[i] + "_" + FeatureTagContext.getDEFAULT();
            }
            com.wangyy.multilanes.core.utils.ReflectionUtils.changeAnnotationValue(kafkaListener, "topics", newTopics);
            log.info("after change, kafka listener topics is {}", kafkaListener.topics());
        }
    }

    private void changeContainerTopic(Object bean) {
        if (!(bean instanceof KafkaMessageListenerContainer)) {
            return;
        }
        try {
            KafkaMessageListenerContainer kafkaMessageListenerContainer = (KafkaMessageListenerContainer) bean;
            Field field = KafkaMessageListenerContainer.class.getSuperclass().getDeclaredField("containerProperties");
            field.setAccessible(true);
            ContainerProperties containerProperties = (ContainerProperties) field.get(kafkaMessageListenerContainer);  // 获取 consumerProperties 对象
            Field topicField = ContainerProperties.class.getSuperclass().getDeclaredField("topics");
            topicField.setAccessible(true);
            String[] originTopic = (String[]) topicField.get(containerProperties);
            String[] newTopics = new String[originTopic.length];
            for (int i = 0; i < originTopic.length; i++) {
                newTopics[i] = originTopic[i] + "_" + FeatureTagContext.getDEFAULT();
            }
            topicField.set(containerProperties, newTopics); // 修改 topics 字段的值
        } catch (Exception e) {
            log.error("change kafka consumer container topic error", e);
        }
    }

    private void addInterceptorToContainer(Object bean) {
        if (!(bean instanceof KafkaMessageListenerContainer)) {
            return;
        }
        KafkaMessageListenerContainer kafkaMessageListenerContainer = (KafkaMessageListenerContainer) bean;
        kafkaMessageListenerContainer.setRecordInterceptor(consumerInterceptor);
    }

    private void addInterceptorToContainerFactory(Object bean) {
        if (!(bean instanceof ConcurrentKafkaListenerContainerFactory)) {
            return;
        }
        ConcurrentKafkaListenerContainerFactory containerFactory = (ConcurrentKafkaListenerContainerFactory) bean;
        containerFactory.setRecordInterceptor(consumerInterceptor);
    }

    private void registerContainerTopicGroupPath(Object bean) {
        if (!(bean instanceof KafkaMessageListenerContainer)) {
            return;
        }
        KafkaMessageListenerContainer container = (KafkaMessageListenerContainer) bean;
        String groupId = container.getGroupId();
        String[] topics = container.getContainerProperties().getTopics();
        for (String topic : topics) {
            nodeWatcher.registerZKPath(KafkaNodeWatcher.path(topic, groupId));
        }
    }

    private void registerAnnotationTopicGroupPath(Object bean) {
        List<Method> kafkaListenerMethods = findKafkaListenerMethods(bean);
        if (CollectionUtils.isEmpty(kafkaListenerMethods)) {
            return;
        }
        log.info("start register annotation consumer zk");
        for (Method method : kafkaListenerMethods) {
            KafkaListener kafkaListener = method.getAnnotation(KafkaListener.class);
            String[] topics = kafkaListener.topics();
            String groupId = findKafkaListenerGroupId(kafkaListener);
            for (String topic : topics) {
                nodeWatcher.registerZKPath(KafkaNodeWatcher.path(topic, groupId));
            }
        }
    }

    private List<Method> findKafkaListenerMethods(Object bean) {
        List<Method> methods = new ArrayList<>();
        ReflectionUtils.doWithMethods(bean.getClass(), methods::add);
        return methods.stream().filter(method -> method.isAnnotationPresent(KafkaListener.class)).collect(Collectors.toList());
    }

    private String findKafkaListenerGroupId(KafkaListener kafkaListener) {
        if (!StringUtils.isEmpty(kafkaListener.groupId())) {
            return kafkaListener.groupId();
        }
        ConcurrentKafkaListenerContainerFactory containerFactory = (ConcurrentKafkaListenerContainerFactory) applicationContext.getBean(kafkaListener.containerFactory());
        return (String) containerFactory.getConsumerFactory().getConfigurationProperties().get(ConsumerConfig.GROUP_ID_CONFIG);
    }
}
