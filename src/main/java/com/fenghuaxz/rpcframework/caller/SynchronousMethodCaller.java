package com.fenghuaxz.rpcframework.caller;

import com.fenghuaxz.rpcframework.CallFuture;
import com.fenghuaxz.rpcframework.Remote;
import com.fenghuaxz.rpcframework.annotations.Timeout;
import com.fenghuaxz.rpcframework.channels.Channel;

import java.lang.reflect.Method;

public class SynchronousMethodCaller extends AbstractMethodCaller {

    public SynchronousMethodCaller(Channel channel) {
        super(channel);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        CallFuture mMethod = new CallFuture(this.mChannel, method, args);
        mMethod.call(Remote.Type.SYNC);
        Timeout timeout = getTimeout(method);
        return mMethod.get(timeout.value(), timeout.unit());
    }
}
