package com.pokutuna.n7logger;

import android.app.*;
import android.content.*;

public class App extends Application {

  private static Context appContext;

  @Override
  public void onCreate() {
    super.onCreate();
    appContext = this;
  }

  public static Context getContext(){
    return appContext;
  }

}
