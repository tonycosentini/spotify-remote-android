package com.endlesswhileloop.spotifyremote.base;

import android.content.Context;
import com.endlesswhileloop.spotifyremote.BuildConfig;
import com.endlesswhileloop.spotifyremote.R;
import com.endlesswhileloop.spotifyremote.SpotifyRemoteService;
import com.endlesswhileloop.spotifyremote.core.data.api.SpotifyAccountApi;
import com.endlesswhileloop.spotifyremote.core.data.api.SpotifyClient;
import com.endlesswhileloop.spotifyremote.core.data.api.SpotifyWebApi;
import com.endlesswhileloop.spotifyremote.core.data.model.SpotifyRemotePreferences;
import com.google.gson.Gson;
import dagger.Module;
import dagger.Provides;
import java.io.File;
import javax.inject.Singleton;
import retrofit.RestAdapter;

@Module(
    injects = {
        SpotifyRemoteApplication.class, SpotifyRemotePreferences.class, SpotifyRemoteService.class
    }, library = true
)
public final class SpotifyRemoteModule {
  private final SpotifyRemoteApplication mApplication;

  public SpotifyRemoteModule(SpotifyRemoteApplication spotifyRemoteApplication) {
    mApplication = spotifyRemoteApplication;
  }

  @Provides Context provideAppContext() {
    return mApplication;
  }

  @Provides SpotifyAccountApi provideSpotifyApi() {
    RestAdapter restAdapter = new RestAdapter.Builder()
        .setLogLevel(BuildConfig.DEBUG ? RestAdapter.LogLevel.FULL : RestAdapter.LogLevel.NONE)
        .setEndpoint(SpotifyAccountApi.HOST)
        .build();

    return restAdapter.create(SpotifyAccountApi.class);
  }

  @Provides SpotifyWebApi provideSpotifyWebApi() {
    RestAdapter restAdapter = new RestAdapter.Builder()
        .setLogLevel(BuildConfig.DEBUG ? RestAdapter.LogLevel.FULL : RestAdapter.LogLevel.NONE)
        .setEndpoint(SpotifyWebApi.HOST)
        .build();

    return restAdapter.create(SpotifyWebApi.class);
  }

  @Provides @Singleton SpotifyClient provideSpotifyApiClient(SpotifyAccountApi spotifyAccountApi, SpotifyWebApi spotifyWebApi, SpotifyRemotePreferences spotifyRemotePreferences) {
    String spotifyClientId = mApplication.getString(R.string.spotify_client_id);
    String spotifyClientSecret = mApplication.getString(R.string.spotify_client_secret);
    return new SpotifyClient(spotifyClientId, spotifyClientSecret, spotifyAccountApi, spotifyWebApi, spotifyRemotePreferences);
  }

  @Provides Gson provideGson() {
    return new Gson();
  }

  @Provides @Singleton SpotifyRemotePreferences provideSpotifyRemotePreferences(Context context, Gson gson) {
    File spotifyTokenFile = new File(context.getFilesDir(), "spotify_token.json");
    return new SpotifyRemotePreferences(gson, spotifyTokenFile);
  }
}
