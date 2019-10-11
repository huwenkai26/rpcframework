package com.fenghuaxz.rpcframework.caller;

import com.fenghuaxz.rpcframework.annotations.Timeout;
import com.fenghuaxz.rpcframework.channels.Channel;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

@Timeout(5)
public abstract class AbstractMethodCaller implements InvocationHandler {

    final Channel mChannel;

    AbstractMethodCaller(Channel channel) {
        this.mChannel = channel;
    }

    Timeout getTimeout(Method method) {
        Timeout timeout;
        if ((timeout = method.getAnnotation(Timeout.class)) == null) {
            if ((timeout = method.getDeclaringClass().getAnnotation(Timeout.class)) == null) {
                timeout = getClass().getAnnotation(Timeout.class);
            }
        }
        return timeout;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Class<?> type = method.getReturnType();
        switch (type.getName()) {
            case "long":
                return (long) 0;
            case "double":
                return (double) 0;
            case "int":
                return 0;
            case "float":
                return (float) 0;
            case "boolean":
                return false;
            case "short":
                return (short) 0;
            case "char":
                return (char) 0;
            case "byte":
                return (byte) 0;
            default:
                return null;
        }
    }
}
