package com.endlesswhileloop.spotifyremote;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.os.SystemClock;
import android.provider.MediaStore;
import com.endlesswhileloop.spotifyremote.base.BaseService;
import com.endlesswhileloop.spotifyremote.core.data.api.SpotifyClient;
import com.endlesswhileloop.spotifyremote.core.data.model.SpotifyPlaylist;
import com.endlesswhileloop.spotifyremote.core.data.model.SpotifyPlaylists;
import com.endlesswhileloop.spotifyremote.core.data.util.ObserverAdapter;
import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;
import dagger.internal.ArrayQueue;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import javax.inject.Inject;
import timber.log.Timber;

public class SpotifyRemoteService extends BaseService {
  @Inject SpotifyClient mSpotifyClient;

  @Override public IBinder onBind(Intent intent) {
    return null;
  }

  private static final UUID PEBBLE_APP_UUID = UUID.fromString("ee12b239-f676-417b-8c07-6bf143b98455");

  private static final int KEY_MESSAGE = 1;
  private static final int KEY_PLAYLIST_NAME = 2;
  private static final int KEY_PLAYLIST_ID = 3;
  private static final int KEY_PLAY_PLAYLIST = 4;

  private static final int VALUE_MESSAGE_CONNECT = 1;
  private static final int VALUE_MESSAGE_DID_RECEIVE_ALL_PLAYLISTS = 4;
  private static final int VALUE_MUST_LAUNCH_SPOTIFY = 5;
  private static final int VALUE_MUST_AUTHORIZE_SPOTIFY = 6;

  private Queue<PebbleDictionary> mMessagesQueue = new ArrayQueue<PebbleDictionary>();

  private List<SpotifyPlaylist> mPlaylists;
  private int mCurrentTransactionId = 1;

  @Override public int onStartCommand(Intent intent, int flags, int startId) {
    return START_STICKY;
  }

  @Override public void onTaskRemoved(Intent rootIntent) {
    // Workaround for gross Kit Kat bug: https://code.google.com/p/android/issues/detail?id=53313
    Intent restartService = new Intent(getApplicationContext(), this.getClass());
    restartService.setPackage(getPackageName());
    PendingIntent restartServicePI = PendingIntent.getService(
        getApplicationContext(),
        1,
        restartService,
        PendingIntent.FLAG_ONE_SHOT
    );
    AlarmManager alarmService = (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);
    alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() +1000, restartServicePI);
  }

  @Override public void onCreate() {
    super.onCreate();

    PebbleKit.registerReceivedAckHandler(this, new PebbleKit.PebbleAckReceiver(PEBBLE_APP_UUID) {
      @Override
      public void receiveAck(Context context, int transactionId) {
        boolean queueWasNotCleared = mMessagesQueue.size() != 0;
        if (queueWasNotCleared) {
          mMessagesQueue.remove();
          sendNextMessage();
        }
      }
    });

    PebbleKit.registerReceivedNackHandler(this, new PebbleKit.PebbleNackReceiver(PEBBLE_APP_UUID) {
      @Override
      public void receiveNack(Context context, int transactionId) {
        sendNextMessage();
      }
    });

    PebbleKit.registerReceivedDataHandler(this, new PebbleKit.PebbleDataReceiver(PEBBLE_APP_UUID) {
      @Override
      public void receiveData(final Context context, final int transactionId, final PebbleDictionary data) {
        PebbleKit.sendAckToPebble(context, transactionId);

        Timber.d("Received value=" + data.getUnsignedInteger(0) + " for key: 0");

        if (data.contains(KEY_MESSAGE)) {
          switch (data.getInteger(KEY_MESSAGE).intValue()) {
            case VALUE_MESSAGE_CONNECT:
              sendPlaylists();
              break;
          }
        } else if (data.contains(KEY_PLAY_PLAYLIST)) {
          int playlistIndex = data.getUnsignedInteger(KEY_PLAY_PLAYLIST).intValue();
          Uri playlistUri = Uri.parse(mPlaylists.get(playlistIndex).mUri);
          final Intent i = new Intent(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH );
          i.setData(playlistUri);
          i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

          startActivity(i);
        }
      }
    });
  }

  private void sendPlaylists() {
    if (!SpotifyAppUtils.isSpotifyRunning(this)) {
      PebbleDictionary messageDictionary = new PebbleDictionary();
      messageDictionary.addInt32(KEY_MESSAGE, VALUE_MUST_LAUNCH_SPOTIFY);
      mMessagesQueue.add(messageDictionary);
      sendNextMessage();
    } else if (mSpotifyClient.isLoggedIn()) {
      mSpotifyClient.getLoggedInUsersPlaylists(new ObserverAdapter<SpotifyPlaylists>() {
        @Override public void onNext(SpotifyPlaylists spotifyPlaylists) {
          sendPlaylistsToPebble(spotifyPlaylists.mItems);
        }
      });
    } else {
      PebbleDictionary messageDictionary = new PebbleDictionary();
      messageDictionary.addInt32(KEY_MESSAGE, VALUE_MUST_AUTHORIZE_SPOTIFY);
      mMessagesQueue.add(messageDictionary);
      sendNextMessage();
    }
  }

  public synchronized void sendPlaylistsToPebble(List<SpotifyPlaylist> playlists) {
    mPlaylists = playlists;
    mMessagesQueue.clear();

    for (SpotifyPlaylist playlist : playlists) {
      PebbleDictionary messageDictionary = new PebbleDictionary();
      messageDictionary.addInt32(KEY_PLAYLIST_ID, mPlaylists.indexOf(playlist));
      messageDictionary.addString(KEY_PLAYLIST_NAME, playlist.mName);
      mMessagesQueue.add(messageDictionary);
    }

    PebbleDictionary finishedDictionary = new PebbleDictionary();
    finishedDictionary.addInt32(KEY_MESSAGE, VALUE_MESSAGE_DID_RECEIVE_ALL_PLAYLISTS);
    mMessagesQueue.add(finishedDictionary);

    sendNextMessage();
  }

  private void sendNextMessage() {
    PebbleDictionary dictionary = mMessagesQueue.peek();
    if (dictionary != null) {
      Timber.d("Sending message #" + mCurrentTransactionId + " to device: " + dictionary.toJsonString());
      PebbleKit.sendDataToPebbleWithTransactionId(getApplicationContext(), PEBBLE_APP_UUID, dictionary, mCurrentTransactionId);
      mCurrentTransactionId++;
    }
  }

}
