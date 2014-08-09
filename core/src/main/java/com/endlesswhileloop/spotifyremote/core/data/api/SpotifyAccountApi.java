package com.endlesswhileloop.spotifyremote.core.data.api;

import com.endlesswhileloop.spotifyremote.core.data.model.SpotifyToken;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.Header;
import retrofit.http.POST;
import rx.Observable;

public interface SpotifyAccountApi {
  public static final String HOST = "https://accounts.spotify.com";

  @FormUrlEncoded @POST("/api/token") Observable<SpotifyToken> token(@Field("grant_type") String grantType, @Field("code") String code,
      @Field("redirect_uri") String redirectUri, @Field("client_id") String clientId, @Field("client_secret") String clientSecret);

  @FormUrlEncoded @POST("/api/token") Observable<SpotifyToken> refreshToken(@Header("Authorization") String authorizationToken, @Field("grant_type") String grantType, @Field("refresh_token") String refreshToken);
}
