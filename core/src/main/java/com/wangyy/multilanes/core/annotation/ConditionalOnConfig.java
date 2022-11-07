package com.wangyy.multilanes.core.annotation;

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

/**
 * 读取.conf文件中指定路径的值v
 * havingValue非空时
 * v == havingValue时满足condition
 * havingValue为空字符串时
 * v != "false"则满足condition
 *
 * 满足condition则向spring容器中注入bean
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
@Conditional(OnConfigCondition.class)
public @interface ConditionalOnConfig {

    /**
     * Alias for {@link #name()}.
     *
     * @return the names
     */
    String[] value() default {};

    String[] name() default {};

    /**
     * The string representation of the expected value for the properties. If not
     * specified, the property must <strong>not</strong> be equals to {@code false}.
     *
     * @return the expected value
     */
    String havingValue() default "";

    /**
     * Specify if the condition should match if the property is not set. Defaults to
     * {@code false}.
     *
     * @return if should match if the property is missing
     */
    boolean matchIfMissing() default false;

    /**
     * If relaxed names should be checked. Defaults to {@code true}.
     *
     * @return if relaxed names are used
     */
    boolean relaxedNames() default true;
}
