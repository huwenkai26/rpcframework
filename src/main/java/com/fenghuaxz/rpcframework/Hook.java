package com.fenghuaxz.rpcframework;

import com.fenghuaxz.rpcframework.channels.Channel;

import java.lang.reflect.Method;

public interface Hook {

    abstract class AbstractMethodExecHook implements Hook {

        protected abstract void beforeExec(Method method, Object[] parameters, Channel channel) throws Exception;

        protected abstract void afterExec(Method method, Channel channel, Object result) throws Exception;

        static void doBefore(Method method, Object[] parameters, Channel channel) throws Exception {
            for (Hook hook : channel.context().hooks()) {
                if (hook instanceof AbstractMethodExecHook) {
                    ((AbstractMethodExecHook) hook).beforeExec(method, parameters, channel);
                }
            }
        }

        static void doAfter(Method method, Object result, Channel channel) throws Exception {
            for (Hook hook : channel.context().hooks()) {
                if (hook instanceof AbstractMethodExecHook) {
                    ((AbstractMethodExecHook) hook).afterExec(method, channel, result);
                }
            }
        }
    }

    abstract class AbstractMethodCallHook implements Hook {

        protected abstract void call(Method method, Object[] parameters, Channel channel, Remote.Type type);

        protected abstract void completed(Method method, Channel channel, Object result, Throwable cause);

        static void doCall(Method method, Object[] parameters, Channel channel, Remote.Type type) {
            channel.runTaskWithContext(() -> {
                for (Hook hook : channel.context().hooks()) {
                    if (hook instanceof AbstractMethodCallHook) {
                        ((AbstractMethodCallHook) hook).call(method, parameters, channel, type);
                    }
                }
            });
        }

        static void doCompleted(Method method, Channel channel, Object result, Throwable cause) {
            channel.runTaskWithContext(() -> {
                for (Hook hook : channel.context().hooks()) {
                    if (hook instanceof AbstractMethodCallHook) {
                        ((AbstractMethodCallHook) hook).completed(method, channel, result, cause);
                    }
                }
            });
        }
    }
}
