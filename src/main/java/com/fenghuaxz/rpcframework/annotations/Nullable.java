package com.fenghuaxz.rpcframework.annotations;

import com.fenghuaxz.rpcframework.Hook;
import com.fenghuaxz.rpcframework.channels.Channel;

import java.lang.annotation.*;
import java.lang.reflect.Method;

@Inherited
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Nullable {

    class EmptyParameterValidator extends Hook.AbstractMethodExecHook {

        @Override
        public void beforeExec(Method method, Object[] parameters, Channel channel) {
            for (int i = 0, j = method.getParameterTypes().length; i < j; i++) {
                if (parameters[i] != null) continue;
                boolean isNullable = false;
                Annotation[] annotations = method.getParameterAnnotations()[i];
                for (Annotation a : annotations) {
                    isNullable = isNullable || a instanceof Nullable;
                }
                if (!isNullable) {
                    throw new IllegalArgumentException(String.format("Parameter %d cannot be null", i + 1));
                }
            }
        }

        @Override
        public void afterExec(Method method, Channel channel, Object result) {
        }
    }
}
