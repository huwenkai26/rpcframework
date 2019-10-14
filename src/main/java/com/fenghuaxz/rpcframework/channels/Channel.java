package com.fenghuaxz.rpcframework.channels;

import com.fenghuaxz.rpcframework.Context;
import com.fenghuaxz.rpcframework.Template;
import com.fenghuaxz.rpcframework.Remote;
import com.fenghuaxz.rpcframework.codec.ByteToMessageCodec;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoop;

import java.net.InetSocketAddress;

public interface Channel extends Remote {

    boolean isActive();

    Context context();

    ChannelFuture close();

    EventLoop eventLoop();

    <T extends ByteToMessageCodec>

    void setCodec(T codec);

    void runTaskWithContext(Runnable task);

    <T extends Template> T template(Class<T> cls);

    ChannelFuture send(Object msg);

    InetSocketAddress remoteAddress();

    InetSocketAddress localAddress();

    interface CloseIntercept {

        void setIntercept(boolean intercept);

        ChannelPromise closePromise();
    }
}
