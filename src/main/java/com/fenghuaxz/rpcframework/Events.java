package com.fenghuaxz.rpcframework;

import com.fenghuaxz.rpcframework.channels.Channel;
import com.fenghuaxz.rpcframework.codec.ByteToMessageCodec;
import com.fenghuaxz.rpcframework.pojo.XRequest;
import com.fenghuaxz.rpcframework.pojo.XResponse;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

import java.lang.reflect.Method;

import static com.fenghuaxz.rpcframework.Hook.AbstractMethodExecHook.doAfter;
import static com.fenghuaxz.rpcframework.Hook.AbstractMethodExecHook.doBefore;

@ChannelHandler.Sharable
final class Events extends ChannelInboundHandlerAdapter {

    static final Events instance = new Events();

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            switch (state) {
                case READER_IDLE:
                    ctx.close();
                    break;

                case WRITER_IDLE:
                    ctx.writeAndFlush(ByteToMessageCodec.PING);
                    break;
            }
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        final Channel channel = (Channel) ctx.channel();

        if (msg instanceof XResponse) {
            CallFuture.doResponse(channel, (XResponse) msg);
            return;
        }

        if (msg instanceof XRequest) {
            channel.runTaskWithContext(() -> {
                final XRequest request = (XRequest) msg;
                final XResponse response = new XResponse();
                response.setId(request.getId());

                Channel.CloseIntercept closeIntercept = (Channel.CloseIntercept) channel;
                closeIntercept.setIntercept(true);

                try {
                    Service.initChannel(channel);
                    final Service service = channel.context().getService(request.getName());
                    final Method method = service.getMethod(request.getMethodName(), request.getParameterTypes());
                    doBefore(method, request.getParameters(), channel);
                    response.setResult(method.invoke(service, request.getParameters()));
                    doAfter(method, response.getResult(), channel);
                } catch (Throwable t) {
                    Throwable cause = t.getCause();
                    response.setCause(cause != null ? cause : t);
                } finally {
                    if (!request.isOneway()) channel.send(response);
                    ChannelPromise closePromise;
                    if ((closePromise = closeIntercept.closePromise()) != null) {
                        ctx.close(closePromise);
                    }
                    closeIntercept.setIntercept(false);
                }
            });
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        final Channel channel = (Channel) ctx.channel();
        channel.runTaskWithContext(() -> {
            for (Template template : channel.context().templates()) {
                template.doActive(channel);
            }
        });
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        final Channel channel;
        CallFuture.doInactive(channel = (Channel) ctx.channel());
        channel.runTaskWithContext(() -> {
            for (Template template : channel.context().templates()) {
                template.doInactive(channel);
            }
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
        final Channel channel = (Channel) ctx.channel();
        channel.runTaskWithContext(() -> {
            for (Template template : channel.context().templates()) {
                template.doCaught(channel, cause);
            }
        });
    }
}
