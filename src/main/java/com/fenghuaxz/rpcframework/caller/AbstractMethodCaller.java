package com.fenghuaxz.rpcframework.caller;

import com.fenghuaxz.rpcframework.annotations.Timeout;
import com.fenghuaxz.rpcframework.channels.Channel;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

@Timeout(5)
abstract class AbstractMethodCaller implements InvocationHandler {

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
}
