package desmund.app.transitionapi;

import android.app.Application;

import io.paperdb.Paper;

public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Paper.init(this);
    }


}
