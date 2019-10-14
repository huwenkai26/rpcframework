package com.fenghuaxz.rpcframework;

import com.fenghuaxz.rpcframework.channels.ChannelFuture;
import com.fenghuaxz.rpcframework.concurrent.IFutureListener;

public interface AsyncHandler<V> extends IFutureListener<ChannelFuture<V>> {

    AsyncHandler<?> CLOSE_ON_FAILURE = (AsyncHandler<Object>) future -> {
        if (!future.isSuccess()) {
            future.channel().close();
        }
    };

    AsyncHandler<?> DO_NOTHING = (AsyncHandler<Object>) future -> {
    };
}
