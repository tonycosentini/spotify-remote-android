package com.endlesswhileloop.spotifyremote.core.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SpotifyPlaylists {
  public @SerializedName("items") List<SpotifyPlaylist> mItems;
}
