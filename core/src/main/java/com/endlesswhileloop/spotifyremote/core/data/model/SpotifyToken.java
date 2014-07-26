package com.endlesswhileloop.spotifyremote.core.data.model;

import com.google.gson.annotations.SerializedName;

public class SpotifyToken {
  public @SerializedName("access_token") String mAccessToken;
  public @SerializedName("token_type") String mTokenType;
  public @SerializedName("expires_in") long mExpiresInSeconds;
  public @SerializedName("refresh_token") String mRefreshToken;
  public long mCreatedAtInMilliseconds = System.currentTimeMillis();

  public boolean isValid() {
    long expirationTimeInMiliseconds = mCreatedAtInMilliseconds + mExpiresInSeconds * 1000;
    return expirationTimeInMiliseconds > System.currentTimeMillis();
  }

  public String getAuthorizationHeaderValue() {
    return mTokenType + " " + mAccessToken;
  }
}
