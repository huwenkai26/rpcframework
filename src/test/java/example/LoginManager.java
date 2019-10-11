package example;

import com.fenghuaxz.rpcframework.annotations.Rpc;

@Rpc
public interface LoginManager {

    int login(String account, String password, int num);
}
