package com.endlesswhileloop.spotifyremote.base;

import android.app.Application;
import com.endlesswhileloop.spotifyremote.BuildConfig;
import dagger.ObjectGraph;
import timber.log.Timber;

public class SpotifyRemoteApplication extends Application {
  private ObjectGraph mObjectGraph;

  @Override public void onCreate() {
    super.onCreate();

    if (BuildConfig.DEBUG) {
      Timber.plant(new Timber.DebugTree());
    } else {
      // TODO Crashlytics.start(this);
      // TODO Timber.plant(new CrashlyticsTree());
    }

    buildObjectGraphAndInject();
  }

  public void buildObjectGraphAndInject() {
    mObjectGraph = ObjectGraph.create(Modules.list(this));
    mObjectGraph.inject(this);
  }

  public ObjectGraph getApplicationGraph() {
    return mObjectGraph;
  }

  public void inject(Object o) {
    mObjectGraph.inject(o);
  }

}
