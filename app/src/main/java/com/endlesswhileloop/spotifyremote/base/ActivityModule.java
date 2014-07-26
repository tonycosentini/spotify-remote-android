package com.endlesswhileloop.spotifyremote.base;

import com.endlesswhileloop.spotifyremote.MainActivity;
import dagger.Module;

@Module(
    injects = MainActivity.class,
    addsTo = SpotifyRemoteModule.class
)
public class ActivityModule {
  private BaseActivity mActivity;

  public ActivityModule(BaseActivity activity) {
    mActivity = activity;
  }
}
