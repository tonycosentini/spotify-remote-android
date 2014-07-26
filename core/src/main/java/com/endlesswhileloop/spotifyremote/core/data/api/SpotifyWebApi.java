package com.endlesswhileloop.spotifyremote.core.data.api;

import com.endlesswhileloop.spotifyremote.core.data.model.SpotifyPlaylists;
import com.endlesswhileloop.spotifyremote.core.data.model.SpotifyUser;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.Path;
import rx.Observable;

public interface SpotifyWebApi {
  public static final String HOST = "https://api.spotify.com";

  @GET("/v1/me") Observable<SpotifyUser> getLoggedInUserProfile(@Header("Authorization") String authorizationToken);

  @GET("/v1/users/{user_id}/playlists") Observable<SpotifyPlaylists> getPlaylists(@Header("Authorization") String authorizationToken, @Path("user_id") String userId);
}
