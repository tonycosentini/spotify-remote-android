package com.endlesswhileloop.spotifyremote;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class StartupReceiver extends BroadcastReceiver {
  @Override public void onReceive(Context context, Intent intent) {
    Intent startServiceIntent = new Intent(context, SpotifyRemoteService.class);
    context.startService(startServiceIntent);
  }
}
