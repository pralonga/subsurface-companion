package org.subsurface;

import java.util.Timer;

import org.subsurface.dao.DatabaseHelper;
import org.subsurface.model.DiveLocationLog;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.j256.ormlite.android.apptools.OpenHelperManager;

/**
 * Service for location retrieval.
 * @author Aurelien PRALONG
 *
 */
public class BackgroundLocationService extends Service implements LocationListener {

	private static final String TAG = "BackgroundLocationService";
	private static final int NOTIFICATION_ID = BackgroundLocationService.class.hashCode();

	private LocationManager locationManager;
	private Timer timer;
	private NotificationManager notificationManager;
	private DatabaseHelper helper;

	@Override
	public void onCreate() {
		super.onCreate();

		// Start DAO
		this.helper = OpenHelperManager.getHelper(this, DatabaseHelper.class);

		// Request position informations
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		int pollDuration;
		try {
			pollDuration = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this).getString("destination_url", getString(R.string.default_duration)));
		} catch (Exception e) {
			pollDuration = Integer.parseInt(getString(R.string.default_duration));
		}
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, pollDuration * 60 * 1000, 0, this);

		// Get NotificationManager
		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		// Show notification
		Notification notif = new Notification(R.drawable.logo, getString(R.string.notification_background_service_on), System.currentTimeMillis());
		Intent notificationIntent = new Intent(this, StartupActivity.class);
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		notif.setLatestEventInfo(getApplicationContext(),
				getString(R.string.app_name),
				getString(R.string.notification_background_service_on), contentIntent);
		notificationManager.notify(NOTIFICATION_ID, notif);

		Log.d(TAG, "Background service started");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		locationManager.removeUpdates(this);
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
		notificationManager.cancel(NOTIFICATION_ID);
		OpenHelperManager.releaseHelper();
	}

	@Override
	public void onLocationChanged(Location location) {
		Log.d(TAG, "Location received");
		try { // Add to current DB
			helper.getDiveDao().create(new DiveLocationLog(location,
					PreferenceManager.getDefaultSharedPreferences(this).getString("background_service_name", getString(R.string.default_dive_name)),
					System.currentTimeMillis()));
		} catch (Exception e) {
			Log.d(TAG, "Could not update dive", e);
		}
	}

	@Override
	public void onProviderDisabled(String provider) {
		Log.d(TAG, String.format("onProviderDisabled(%s)", provider));
	}

	@Override
	public void onProviderEnabled(String provider) {
		Log.d(TAG, String.format("onProviderEnabled(%s)", provider));
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		Log.d(TAG, String.format("onStatusChanged(%s, %d)", provider, status));
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
