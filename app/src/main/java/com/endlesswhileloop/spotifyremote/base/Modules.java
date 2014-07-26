package com.endlesswhileloop.spotifyremote.base;

final class Modules {
  static Object[] list(SpotifyRemoteApplication app) {
    return new Object[] {
        new SpotifyRemoteModule(app),
    };
  }

  private Modules() {
    // No instances.
  }
}