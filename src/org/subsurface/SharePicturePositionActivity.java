package org.subsurface;

import java.util.Calendar;
import java.util.Date;

import org.subsurface.controller.DiveController;
import org.subsurface.model.DiveLocationLog;
import org.subsurface.util.DateUtils;
import org.subsurface.util.GpsUtil;
import org.subsurface.ws.WsException;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class SharePicturePositionActivity extends SherlockActivity {

	private static final String TAG = "SharePicturePositionActivity";

	private final DiveLocationLog dive = new DiveLocationLog();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.dive_detail_edit);

		if (DiveController.instance.getHelper() == null) {
			try {
				DiveController.instance.setContext(this);
			} catch (Exception e) {
				new AlertDialog.Builder(this)
						.setTitle(R.string.error_title)
						.setMessage(R.string.error_fatal)
						.setPositiveButton(android.R.string.ok,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										finish();
									}
								}).setCancelable(false).show();
			}
		}

		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		String action = intent.getAction();

		if (Intent.ACTION_SEND.equals(action)) {
			if (extras.containsKey(Intent.EXTRA_STREAM)) {
				try {
					// Get image path
					Uri uri = (Uri) extras.getParcelable(Intent.EXTRA_STREAM);
					String[] proj = { MediaStore.Images.Media.DATA };
					Cursor cursor = getContentResolver().query(uri, proj, null, null, null);
					int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
					cursor.moveToFirst();
					String path = cursor.getString(column_index);
					cursor.close();
					ExifInterface exif = new ExifInterface(path);

					// Timestamp
					long timestamp = DateUtils.getFakeUtcDate();
					try {
						Calendar tsCal = Calendar.getInstance();
						tsCal.setTime(DateUtils.initGMT("yyyy:MM:dd HH:mm:ss")
								.parse(exif.getAttribute(ExifInterface.TAG_DATETIME)));
						tsCal.set(Calendar.SECOND, 0);
						timestamp = tsCal.getTimeInMillis();
					} catch (Exception ignored) {}
					dive.setTimestamp(timestamp);

					// Location
					float[] coords = new float[2];
					if (exif.getLatLong(coords)) { // OK, refresh display
						dive.setLatitude(coords[0]);
						dive.setLongitude(coords[1]);
						((TextView) findViewById(R.id.coordinates)).setText(
								GpsUtil.buildCoordinatesString(this, dive.getLatitude(), dive.getLongitude()));
						((TextView) findViewById(R.id.date)).setText(
								DateUtils.initGMT(getString(R.string.date_format_full)).format(new Date(dive.getTimestamp())));
					} else {
						Log.d(TAG, "Could not retrieve image location");
						Toast.makeText(SharePicturePositionActivity.this, R.string.error_no_image_location, Toast.LENGTH_LONG).show();
						finish();
					}
				} catch (Exception e) {
					Log.d(TAG, "Could not open image", e);
					Toast.makeText(SharePicturePositionActivity.this, R.string.error_open_image, Toast.LENGTH_LONG).show();
					finish();
				}
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.share_picture, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (dive == null) {
			return false;
		}
		MenuItem mi = menu.findItem(R.id.menu_map);
		// disable "Show on map" menu item
		// if no geo URI activities exist
		boolean hasActivities = getPackageManager().queryIntentActivities(GpsUtil.getGeoIntent(dive.getLatitude(), dive.getLongitude()), 0).size() != 0;
		mi.setEnabled(hasActivities);
		mi.setVisible(hasActivities);
		return true;
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		// Update name
		dive.setName(((EditText) findViewById(R.id.title)).getText().toString());

		// Perform action
		if (item.getItemId() == R.id.menu_map) {
			startActivity(GpsUtil.getGeoIntent(dive.getLatitude(), dive.getLongitude()));
		} else if (item.getItemId() == R.id.menu_send) {
			new Thread(new Runnable() {
				public void run() {
					int messageCode = R.string.error_send;
					try {
						DiveController.instance.sendDiveLog(dive);
						messageCode = R.string.confirmation_location_sent;
					} catch (WsException e) {
						messageCode = e.getCode();
					} catch (Exception e) {
						Log.d(TAG, "Could not send dive " + dive.getName(), e);
					}
					final String message = getString(messageCode, dive.getName());
					runOnUiThread(new Runnable() {
						public void run() {
							Toast.makeText(SharePicturePositionActivity.this, message, Toast.LENGTH_SHORT).show();
							finish();
						}
					});
				}
			}).start();
		} else if (item.getItemId() == R.id.menu_delete) {
			finish();
		} else if (item.getItemId() == R.id.menu_save) {
			DiveController.instance.updateDiveLog(dive);
			finish();
		} else if (item.getItemId() == R.id.menu_settings) {
    		startActivity(new Intent(this, Preferences.class));
    	}
		return true;
	}
}
