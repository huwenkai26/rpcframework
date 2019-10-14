package example;

import com.fenghuaxz.rpcframework.*;
import com.fenghuaxz.rpcframework.channels.ChannelFuture;

import java.net.InetSocketAddress;

public class AppMain {

    public static void main(String[] args) throws Exception {

        RPCServer server = new RPCServer().bind(new InetSocketAddress(8000));
        server.addService(new LoginManagerImpl());

        RPCClient client = new RPCClient().setAddress(new InetSocketAddress(8000)).connect();


        AsyncHandler listener = new AsyncHandler<Integer>() {
            AsyncHandler instance;

            @Override
            public void completed(ChannelFuture<Integer> future) {
                if (instance == null) instance = this;
                if (future.isSuccess()) {
                    System.out.println("响应: " + future.getNow());

                    try {
                        client.async(LoginManager.class, instance).login("a", "p", future.getNow() + 1);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    future.cause().printStackTrace();
                }
            }
        };
        client.async(LoginManager.class, listener).login("a", "p", 0);
    }
}
