package com.endlesswhileloop.spotifyremote;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import com.endlesswhileloop.spotifyremote.base.BaseActivity;
import com.endlesswhileloop.spotifyremote.core.data.api.SpotifyClient;
import com.endlesswhileloop.spotifyremote.core.data.model.SpotifyToken;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import javax.inject.Inject;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import rx.Observer;

public class MainActivity extends BaseActivity {
  @Inject SpotifyClient mSpotifyClient;

  @InjectView(R.id.main_progressbar) ProgressBar mProgressBar;
  @InjectView(R.id.main_viewgroup_logged_out) ViewGroup mViewGroupLoggedOut;
  @InjectView(R.id.main_viewgroup_logged_in) ViewGroup mViewGroupLoggedIn;
  @InjectView(R.id.main_viewgroup_spotify_not_running) ViewGroup mViewGroupSpotifyNotRunning;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ButterKnife.inject(this);
    startService(new Intent(this, SpotifyRemoteService.class));
    updateAccountState();
  }

  @Override protected void onResume() {
    super.onResume();
    updateAccountState();
    handleSpotifyUri();
  }

  @OnClick(R.id.main_button_start) public void onStartButton() {
    Intent i = new Intent(Intent.ACTION_VIEW);
    i.setData(Uri.parse(mSpotifyClient.getAuthorizationUrl()));
    startActivity(i);
  }

  @OnClick(R.id.main_button_log_out) public void onLogOutButton() {
    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
    alertBuilder.setMessage(R.string.are_you_sure_you_want_to_log_out);
    alertBuilder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
      @Override public void onClick(DialogInterface dialogInterface, int i) {
        mSpotifyClient.logOut();
        updateAccountState();
      }
    });
    alertBuilder.setNegativeButton(R.string.cancel, null);
    alertBuilder.show();
  }

  @OnClick(R.id.main_button_launch_spotify) public void onLaunchSpotify() {
    SpotifyAppUtils.launchSpotify(this);
  }

  @OnClick(R.id.main_button_download_watchapp) public void onDownloadWatchApp() {
    Intent i = new Intent(Intent.ACTION_VIEW);
    i.setData(Uri.parse("pebble://appstore/53d88b3c909b059949000146"));
    startActivity(i);
  }

  private void updateAccountState() {
    mProgressBar.setVisibility(View.GONE);

    boolean loggedIn = mSpotifyClient.isLoggedIn();
    mViewGroupLoggedIn.setVisibility(loggedIn ? View.VISIBLE : View.GONE);
    mViewGroupLoggedOut.setVisibility(loggedIn ? View.GONE : View.VISIBLE);
    mViewGroupSpotifyNotRunning.setVisibility(SpotifyAppUtils.isSpotifyRunning(this) ? View.GONE : View.VISIBLE);
  }

  private void showAuthorizationErrorDialog() {
    mProgressBar.setVisibility(View.GONE);
    mViewGroupLoggedOut.setVisibility(View.VISIBLE);

    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
    alertDialogBuilder.setTitle(R.string.error_authorizing_account_title);
    alertDialogBuilder.setMessage(R.string.error_authorizing_account_description);
    alertDialogBuilder.setNeutralButton(R.string.ok, null);
    alertDialogBuilder.show();
  }

  private void handleSpotifyUri() {
    Uri uri = getIntent().getData();
    if (getIntent().getData() != null && !mSpotifyClient.isLoggedIn()) {
      mProgressBar.setVisibility(View.VISIBLE);
      mViewGroupLoggedOut.setVisibility(View.GONE);

      try {
        List<NameValuePair> parameters = URLEncodedUtils.parse(new URI(uri.toString()), "UTF-8");

        // TODO: Add state checking here too.

        for (NameValuePair nameValuePair : parameters) {
          if (nameValuePair.getName().equals("code")) {
            String authorizationCode = nameValuePair.getValue();
            mSpotifyClient.exchangeAuthorizationCode(authorizationCode, new Observer<SpotifyToken>() {
              @Override public void onCompleted() {

              }

              @Override public void onError(Throwable e) {
                showAuthorizationErrorDialog();
              }

              @Override public void onNext(SpotifyToken spotifyToken) {
                updateAccountState();
              }
            });
          } else if (nameValuePair.getName().equals("error")) {
            showAuthorizationErrorDialog();
          }
        }
      } catch (URISyntaxException e) {
        throw new RuntimeException(e);
      }
    }
  }
}