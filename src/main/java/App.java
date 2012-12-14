package com.pokutuna.wdlogger;

import android.app.*;
import android.content.*;
import android.content.pm.*;

public class App extends Application {

  private static Context appContext;

  @Override
  public void onCreate() {
    super.onCreate();
    appContext = this;
  }

  public static Context getContext() {
    return appContext;
  }

}
