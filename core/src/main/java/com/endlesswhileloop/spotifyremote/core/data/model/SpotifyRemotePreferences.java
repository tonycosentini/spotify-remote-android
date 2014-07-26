package com.endlesswhileloop.spotifyremote.core.data.model;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class SpotifyRemotePreferences {
  private final Gson mGson;
  private final File mSpotifyTokenFile;

  public SpotifyRemotePreferences(Gson gson, File spotifyTokenFile) {
    mGson = gson;
    mSpotifyTokenFile = spotifyTokenFile;
  }

  public void saveSpotifyToken(SpotifyToken spotifyToken) {
    String tokenJson = mGson.toJson(spotifyToken);

    try {
      mSpotifyTokenFile.createNewFile();

      FileOutputStream fileOutputStream = new FileOutputStream(mSpotifyTokenFile);
      OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
      outputStreamWriter.append(tokenJson);
      outputStreamWriter.close();
      outputStreamWriter.close();

      fileOutputStream.close();
    } catch (IOException e) {
      throw new RuntimeException("Error writing SpotifyToken file to disk.", e);
    }
  }

  public SpotifyToken getSpotifyToken() {
    if (!mSpotifyTokenFile.exists()) {
      return null;
    }

    try {
      FileInputStream fileInputStream = new FileInputStream(mSpotifyTokenFile);
      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
      StringBuilder sb = new StringBuilder();
      String line;
      while ((line = bufferedReader.readLine()) != null) {
        sb.append(line);
      }

      String json = sb.toString();
      bufferedReader.close();
      fileInputStream.close();

      return mGson.fromJson(json, SpotifyToken.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void clearSpotifyToken() {
    if (!mSpotifyTokenFile.delete()) {
      throw new RuntimeException("Unable to delete Spotify account credentials.");
    }
  }
}
