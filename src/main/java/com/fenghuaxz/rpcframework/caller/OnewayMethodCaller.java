package com.fenghuaxz.rpcframework.caller;

import com.fenghuaxz.rpcframework.Context;
import com.fenghuaxz.rpcframework.WriteTask;
import com.fenghuaxz.rpcframework.Remote;
import com.fenghuaxz.rpcframework.channels.Channel;

import java.lang.reflect.Method;

public class OnewayMethodCaller extends AbstractMethodCaller {

    public OnewayMethodCaller(Channel channel) {
        super(channel);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        new WriteTask(this.mChannel, method, args).write(Remote.Type.ONEWAY);
        return Context.takeTypeDefaultValue(method.getReturnType());
    }
}
