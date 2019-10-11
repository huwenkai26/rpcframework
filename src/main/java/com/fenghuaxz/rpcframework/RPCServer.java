package com.fenghuaxz.rpcframework;

import com.fenghuaxz.rpcframework.channels.NioServerSocketChannel;
import com.fenghuaxz.rpcframework.codec.ByteToMessageCodec;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

public class RPCServer extends Context {

    private final EventExecutorGroup eventExecutors;
    private final EventLoopGroup bossGroup = newIOGroup();
    private final EventLoopGroup workerGroup = newIOGroup();
    private final ServerBootstrap sb = new ServerBootstrap();

    public RPCServer() {
        this(new DefaultEventExecutorGroup(Runtime.getRuntime().availableProcessors() * 2));
    }

    public RPCServer(EventExecutorGroup eventExecutors) {
        if (eventExecutors == null) {
            throw new NullPointerException("eventExecutors");
        }
        this.eventExecutors = eventExecutors;
        this.sb.group(bossGroup, workerGroup);
        this.sb.channelFactory(() -> new NioServerSocketChannel(this));
        option(ChannelOption.ALLOCATOR, ByteBufAllocator.DEFAULT);
        option(ChannelOption.SO_BACKLOG, 128);
        childOption(ChannelOption.ALLOCATOR, ByteBufAllocator.DEFAULT);
        childOption(ChannelOption.TCP_NODELAY, true);
        this.sb.childHandler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) {
                final ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast(new ByteToMessageCodec());
                pipeline.addLast(new IdleStateHandler(10, 0, 0, TimeUnit.SECONDS));
                pipeline.addLast(eventExecutors, Events.instance);
            }
        });
    }

    public <T> RPCServer option(ChannelOption<T> option, T value) {
        this.sb.option(option, value);
        return this;
    }

    public <T> RPCServer childOption(ChannelOption<T> option, T value) {
        this.sb.childOption(option, value);
        return this;
    }

    public RPCServer bind(InetSocketAddress address) throws Exception {
        final Thread thread = Thread.currentThread();
        final AtomicReference<Exception> err = new AtomicReference<>();
        new Thread(getClass().getSimpleName() + " - " + address) {
            @Override
            public void run() {
                try {
                    final ChannelFuture future = sb.bind(address).sync();
                    LockSupport.unpark(thread);
                    future.channel().closeFuture().sync();
                } catch (Exception e) {
                    err.set(e);
                    LockSupport.unpark(thread);
                } finally {
                    dispose();
                }
            }
        }.start();
        LockSupport.park();
        Exception cause;
        if ((cause = err.get()) != null) throw cause;
        return this;
    }

    @Override
    public void dispose() {
        this.bossGroup.shutdownGracefully();
        this.workerGroup.shutdownGracefully();
        this.eventExecutors.shutdownGracefully();
    }

    private static EventLoopGroup newIOGroup() {
        boolean isLinux = System.getProperty("os.name").toLowerCase().contains("linux");
        return isLinux ? new EpollEventLoopGroup() : new NioEventLoopGroup();
    }
}