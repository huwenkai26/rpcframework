package example;

import com.fenghuaxz.rpcframework.AsynchronousHandler;
import com.fenghuaxz.rpcframework.RPCClient;
import com.fenghuaxz.rpcframework.RPCServer;
import com.fenghuaxz.rpcframework.channels.ChannelFuture;
import com.fenghuaxz.rpcframework.concurrent.IFuture;
import com.fenghuaxz.rpcframework.concurrent.IFutureListener;

import java.net.InetSocketAddress;

public class AppMain {

    public static void main(String[] args) throws Exception {

        RPCServer server = new RPCServer().bind(new InetSocketAddress(8000));
        server.addService(new LoginManagerImpl());


        RPCClient client = new RPCClient().setAddress(new InetSocketAddress(8000)).connect();

        AsynchronousHandler handler = new AsynchronousHandler<Integer>() {
            AsynchronousHandler instance;

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
        client.async(LoginManager.class, handler).login("a", "p", 0);
    }
}
