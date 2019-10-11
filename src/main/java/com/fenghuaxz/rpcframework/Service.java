package com.fenghuaxz.rpcframework;

import com.fenghuaxz.rpcframework.channels.Channel;
import io.netty.util.concurrent.FastThreadLocal;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.WeakHashMap;

public abstract class Service {

    private final Map<String, Method> mMethods = new WeakHashMap<>();

    final Method getMethod(String name, Class<?>[] parameterTypes) throws NoSuchMethodException {
        final String key = name + Arrays.toString(parameterTypes);
        Method method;
        if ((method = mMethods.get(key)) == null) {
            method = getClass().getMethod(name, parameterTypes);
            this.mMethods.put(key, method);
        }
        return method;
    }

    protected Template[] newTemplates() {
        return new Template[0];
    }

    protected final Channel channel() {
        final Channel channel;
        if ((channel = ChannelHolder.getChannel()) == null) {
            throw new IllegalStateException("Please use in the event thread.");
        }
        return channel;
    }

    static void initChannel(Channel channel) {
        ChannelHolder.setChannel(channel);
    }

    private static class ChannelHolder extends FastThreadLocal<Channel> {

        private static final ChannelHolder instance = new ChannelHolder();

        static Channel getChannel() {
            return instance.get();
        }

        static void setChannel(Channel channel) {
            instance.set(channel);
        }
    }
}