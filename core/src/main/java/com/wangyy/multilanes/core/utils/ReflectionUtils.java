package com.wangyy.multilanes.core.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

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
}
