package com.endlesswhileloop.spotifyremote.base;

import android.app.Activity;
import android.os.Bundle;
import dagger.ObjectGraph;
import java.util.Arrays;
import java.util.List;

public abstract class BaseActivity extends Activity {
  private ObjectGraph mActivityGraph;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    SpotifyRemoteApplication application = ((SpotifyRemoteApplication)getApplication());

    mActivityGraph = application.getApplicationGraph().plus(getModules().toArray());
    mActivityGraph.inject(this);
  }

  @Override protected void onDestroy() {
    // Eagerly clear the reference to the activity graph to allow it to be garbage collected as
    // soon as possible.
    mActivityGraph = null;

    super.onDestroy();
  }

  protected List<Object> getModules() {
    return Arrays.<Object>asList(new ActivityModule(this));
  }

  /** Inject the supplied {@code object} using the activity-specific graph. */
  public void inject(Object object) {
    mActivityGraph.inject(object);
  }
}
