package com.endlesswhileloop.spotifyremote.core.data.api;

import com.endlesswhileloop.spotifyremote.core.data.model.SpotifyRemotePreferences;
import com.endlesswhileloop.spotifyremote.core.data.model.SpotifyToken;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SpotifyClientTest {
  private SpotifyClient mSpotifyClient;

  @Test public void isLoggedInShouldReturnTruWhenLoggedIn() {
    setupSpotifyClient(mock(SpotifyToken.class));
    assertTrue(mSpotifyClient.isLoggedIn());
  }

  @Test public void isLoggedInShouldReturnFalseWhenNotLoggedIn() {
    setupSpotifyClient(null);
    assertFalse(mSpotifyClient.isLoggedIn());
  }

  @Test public void logOutShouldClearToken() {
    setupSpotifyClient(mock(SpotifyToken.class));
    mSpotifyClient.logOut();
    assertFalse(mSpotifyClient.isLoggedIn());
  }

  private void setupSpotifyClient(SpotifyToken spotifyToken) {
    SpotifyRemotePreferences preferences = mock(SpotifyRemotePreferences.class);
    when(preferences.getSpotifyToken()).thenReturn(spotifyToken);

    mSpotifyClient = new SpotifyClient(
        "dummy client id",
        "dummy client secret",
        mock(SpotifyAccountApi.class),
        mock(SpotifyWebApi.class),
        preferences
    );
  }
}
