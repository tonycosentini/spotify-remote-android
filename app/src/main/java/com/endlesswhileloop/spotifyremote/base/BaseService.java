package com.endlesswhileloop.spotifyremote.base;

import android.app.Service;

public abstract class BaseService extends Service {
  @Override public void onCreate() {
    super.onCreate();

    ((SpotifyRemoteApplication)getApplication()).inject(this);
  }
}
