package com.wangyy.multilanes.core.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.Map;

public abstract class ReflectionUtils {

    public static void setField(Field field, Object value, Object bean) throws Exception {
        if (value == null) {
            return;
        }
        field.setAccessible(true);
        Field modifiers = field.getClass().getDeclaredField("modifiers");
        modifiers.setAccessible(true);
        modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);

        field.set(bean, value);
        modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.setAccessible(false);
    }

    public static Object changeAnnotationValue(Annotation annotation, String key, Object newValue) {
        Object handler = Proxy.getInvocationHandler(annotation);
        Field f;
        try {
            f = handler.getClass().getDeclaredField("memberValues");
        } catch (NoSuchFieldException | SecurityException e) {
            throw new IllegalStateException(e);
        }
        f.setAccessible(true);
        Map<String, Object> memberValues;
        try {
            memberValues = (Map<String, Object>) f.get(handler);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
        Object oldValue = memberValues.get(key);
        if (oldValue == null || oldValue.getClass() != newValue.getClass()) {
            throw new IllegalArgumentException("illegal..");
        }
        memberValues.put(key, newValue);
        return oldValue;
    }
}
