package com.endlesswhileloop.spotifyremote.core.data.api;

import com.endlesswhileloop.spotifyremote.core.data.model.SpotifyPlaylists;
import com.endlesswhileloop.spotifyremote.core.data.model.SpotifyRemotePreferences;
import com.endlesswhileloop.spotifyremote.core.data.model.SpotifyToken;
import com.endlesswhileloop.spotifyremote.core.data.model.SpotifyUser;
import com.google.common.io.BaseEncoding;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class SpotifyClient {
  private static final String REDIRECT_URI = "spotifyremote://received_credentials";

  private final String mClientId;
  private final String mClientSecret;
  private final SpotifyAccountApi mSpotifyAccountApi;
  private final SpotifyWebApi mSpotifyWebApi;
  private final SpotifyRemotePreferences mSpotifyRemotePreferences;

  private SpotifyToken mCurrentSpotifyToken;

  public SpotifyClient(String clientId, String clientSecret, SpotifyAccountApi spotifyAccountApi, SpotifyWebApi spotifyWebApi, SpotifyRemotePreferences spotifyRemotePreferences) {
    mClientId = clientId;
    mClientSecret = clientSecret;
    mSpotifyAccountApi = spotifyAccountApi;
    mSpotifyWebApi = spotifyWebApi;
    mSpotifyRemotePreferences = spotifyRemotePreferences;

    mCurrentSpotifyToken = mSpotifyRemotePreferences.getSpotifyToken();
  }

  public String getAuthorizationUrl() {
    try {
      return String.format("https://accounts.spotify.com/authorize/?client_id=%s&response_type=code&redirect_uri=%s&scope=%s", mClientId,
          URLEncoder.encode(REDIRECT_URI, "UTF-8"), URLEncoder.encode("playlist-read-private user-read-private", "UTF-8"));
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  public boolean isLoggedIn() {
    return mCurrentSpotifyToken != null;
  }

  public void exchangeAuthorizationCode(String authorizationCode, Observer<SpotifyToken> observer) {
    mSpotifyAccountApi.token("authorization_code", authorizationCode, REDIRECT_URI, mClientId, mClientSecret)
        .subscribeOn(Schedulers.io())
        .doOnNext(new Action1<SpotifyToken>() {
          @Override public void call(SpotifyToken spotifyToken) {
            mCurrentSpotifyToken = spotifyToken;
            mSpotifyRemotePreferences.saveSpotifyToken(mCurrentSpotifyToken);
          }
        })
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(observer);
  }

  public void getLoggedInUsersPlaylists(Observer<SpotifyPlaylists> observer) {
    updateTokenIfNecessary().flatMap(new Func1<SpotifyToken, Observable<SpotifyUser>>() {
      @Override public Observable<SpotifyUser> call(SpotifyToken spotifyToken) {
        return mSpotifyWebApi.getLoggedInUserProfile(spotifyToken.getAuthorizationHeaderValue());
      }
    }).flatMap(new Func1<SpotifyUser, Observable<SpotifyPlaylists>>() {
      @Override public Observable<SpotifyPlaylists> call(SpotifyUser spotifyUser) {
        return mSpotifyWebApi.getPlaylists(mCurrentSpotifyToken.getAuthorizationHeaderValue(), spotifyUser.mId);
      }
    }).observeOn(AndroidSchedulers.mainThread()).subscribe(observer);
  }

  public void logOut() {
    mSpotifyRemotePreferences.clearSpotifyToken();
    mCurrentSpotifyToken = null;
  }


  private Observable<SpotifyToken> updateTokenIfNecessary() {
    if (mCurrentSpotifyToken.isValid()) {
      return Observable.create(new Observable.OnSubscribe<SpotifyToken>() {
        @Override public void call(Subscriber<? super SpotifyToken> subscriber) {
          subscriber.onNext(mCurrentSpotifyToken);
          subscriber.onCompleted();
        }
      });
    } else {
      String clientAndSecretBase64 = BaseEncoding.base64().encode((String.format("%s:%s", mClientId, mClientSecret).getBytes()));
      String clientAndSecretHeader = String.format("Basic %s", clientAndSecretBase64);
      return mSpotifyAccountApi.refreshToken(clientAndSecretHeader, "refresh_token", mCurrentSpotifyToken.mRefreshToken).doOnNext(new Action1<SpotifyToken>() {
        @Override public void call(SpotifyToken spotifyToken) {
          if (spotifyToken.mRefreshToken == null) {
            spotifyToken.mRefreshToken = mCurrentSpotifyToken.mRefreshToken;
          }
          mCurrentSpotifyToken = spotifyToken;
        }
      });
    }
  }
}
