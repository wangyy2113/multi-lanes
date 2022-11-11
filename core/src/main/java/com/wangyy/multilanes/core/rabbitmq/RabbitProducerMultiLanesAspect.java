package com.wangyy.multilanes.core.rabbitmq;

import com.wangyy.multilanes.core.annotation.ConditionalOnConfig;
import com.wangyy.multilanes.core.trace.FeatureTagContext;
import com.wangyy.multilanes.core.utils.FeatureTagUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.CodeSignature;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/*
 * Rabbit Producer 发送消息时，exchange打上featureTag
 *
 *
 */
@ConditionalOnConfig("multi-lanes.rabbit.enable")
@Slf4j
@Aspect
@Component
public class RabbitProducerMultiLanesAspect {

    private static final String EXCHANGE = "exchange";

    private static final String EXCHANGE_ARG = "exchangeArg";

    @Around("execution(* org.springframework.amqp.rabbit.core.RabbitTemplate.convertAndSend(..))"
            + "|| execution(* org.springframework.amqp.rabbit.core.RabbitTemplate.send(..))"
            + "|| execution(* org.springframework.amqp.rabbit.core.RabbitTemplate.sendAndReceive(..))"
            + "|| execution(* org.springframework.amqp.rabbit.core.RabbitTemplate.convertSendAndReceive(..))"
            + "|| execution(* org.springframework.amqp.rabbit.core.RabbitTemplate.convertSendAndReceiveAsType(..))"
            + "|| execution(* org.springframework.amqp.rabbit.core.RabbitTemplate.doSend(..))")
    public Object process(ProceedingJoinPoint pjp) throws Throwable {
        String[] paramNames = ((CodeSignature) pjp.getSignature()).getParameterNames();
        Object[] args = null;
        if (paramNames != null) {
            for (int i = 0; i < paramNames.length; i++) {
                String exchangeParamName = paramNames[i];
                if (!EXCHANGE.equals(exchangeParamName) && !EXCHANGE_ARG.equals(exchangeParamName)) {
                    continue;
                }
                args = pjp.getArgs();
                if (!(args[i] instanceof String)) {
                    log.error("exchangeParamValue does not instanceof String");
                    break;
                }
                String exchangeParamValue = args[i].toString();
                if (StringUtils.isEmpty(exchangeParamValue)) {
                    break;
                }
                //TODO base-line自发流量处理，这里暂时处理成base-line自发流量都去base-line
                if (FeatureTagContext.isBaseLine()) {
                    break;
                }
                String mockExchangeParam = mockExchangeParam(exchangeParamValue);
                args[i] = mockExchangeParam;
            }
        }
        if (args == null) {
            return pjp.proceed();
        }
        return pjp.proceed(args);
    }

    private String mockExchangeParam(String exchangeParam) {
        String ft = FeatureTagContext.get();
        if (exchangeParam.endsWith(ft)) {
            return exchangeParam;
        }
        return FeatureTagUtils.buildWithFeatureTag(exchangeParam, ft);
    }


}
