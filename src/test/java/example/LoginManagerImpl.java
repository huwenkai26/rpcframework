package example;

import com.fenghuaxz.rpcframework.Template;
import com.fenghuaxz.rpcframework.Service;

public class LoginManagerImpl extends Service implements LoginManager {

    @Override
    protected Template[] newTemplates() {
        return super.newTemplates();
    }

    @Override
    public int login(String account, String password, int num) {
        System.out.println("登录: "+channel());
//        channel().close();
        return num;
    }
}
