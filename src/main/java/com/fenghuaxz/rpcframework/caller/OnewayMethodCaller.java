package com.fenghuaxz.rpcframework.caller;

import com.fenghuaxz.rpcframework.CallFuture;
import com.fenghuaxz.rpcframework.Remote;
import com.fenghuaxz.rpcframework.channels.Channel;

import java.lang.reflect.Method;

public class OnewayMethodCaller extends AbstractMethodCaller {

    public OnewayMethodCaller(Channel channel) {
        super(channel);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        new CallFuture(this.mChannel, method, args).call(Remote.Type.ONEWAY);
        return super.invoke(proxy, method, args);
    }
}
