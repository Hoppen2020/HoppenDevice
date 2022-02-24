package co.hoppen.devicedemo;

import android.app.Application;

/**
 * Created by YangJianHui on 2021/9/25.
 */
public class APP extends Application {
    public static APP app;

    @Override
    public void onCreate() {
        super.onCreate();
        this.app = this;

    }


}
