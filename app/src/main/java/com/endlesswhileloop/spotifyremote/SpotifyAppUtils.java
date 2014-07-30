package com.endlesswhileloop.spotifyremote;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import java.util.List;

public class SpotifyAppUtils {
  private static final String SPOTIFY_APP_PACKAGE = "com.spotify.music";

  public static boolean isSpotifyRunning(Context context) {
    ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    List<ActivityManager.RunningAppProcessInfo> procInfos = activityManager.getRunningAppProcesses();
    for (int i = 0; i < procInfos.size(); i++) {
      if (procInfos.get(i).processName.equals(SPOTIFY_APP_PACKAGE)) {
        return true;
      }
    }

    return false;
  }

  public static void launchSpotify(Context context) {
    Intent intent = context.getPackageManager().getLaunchIntentForPackage(SPOTIFY_APP_PACKAGE);
    if (intent != null)
    {
    /* we found the activity now start the activity */
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      context.startActivity(intent);
    }
  }
}
