package com.endlesswhileloop.spotifyremote.base;

import com.crashlytics.android.Crashlytics;
import timber.log.Timber;

public class CrashlyticsTree extends Timber.HollowTree implements Timber.TaggedTree {

  private String nextTag;

  @Override public void tag(String tag) {
    nextTag = tag;
  }

  private String getTag() {
    String tag = nextTag;
    if (tag != null) {
      nextTag = null;
    }

    return tag;
  }

  @Override public void i(String message, Object... args) {
    log("INFO", message, args);
  }

  @Override public void i(Throwable t, String message, Object... args) {
    log("INFO", message, args);
  }

  @Override public void w(String message, Object... args) {
    log("WARN", message, args);
  }

  @Override public void w(Throwable t, String message, Object... args) {
    log("WARN", message, args);
  }

  @Override public void e(String message, Object... args) {
    log("ERROR", message, args);
  }

  @Override public void e(Throwable t, String message, Object... args) {
    e(message, args);
    Crashlytics.logException(t);
  }

  private void log(String caller, String message, Object... args) {
    if (message == null) return;
    StringBuilder s = new StringBuilder();
    String tag = getTag();
    if (tag != null) {
      s.append("[").append(tag).append("] ");
    }
    s.append(caller).append(" - ").append(String.format(message, args));
    Crashlytics.log(s.toString());
  }
}
