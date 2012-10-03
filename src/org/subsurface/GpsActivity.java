/* Subsurface for Android
 * Copyright (C) 2012  Aurelien PRALONG
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.subsurface;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.subsurface.dao.DbAdapter;
import org.subsurface.dao.DiveLocationLogDao;
import org.subsurface.model.DiveLocationLog;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Activity for location operations.
 * @author Aurelien PRALONG
 *
 */
public class GpsActivity extends Activity {

	private LocationManager locationManager;
	private EditText locationName;
	private DiveLocationLogDao locationDao;

	/**
	 * Builds URL for a log.
	 * @param log log to transmit
	 * @return URL to call, or null if settings are not available
	 */
	public String getSendUrl(DiveLocationLog log) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String destUrl = prefs.getString("destination_url", null);
		String userId = prefs.getString("user_id", null);

		String url = null;
		if (destUrl != null && userId != null) { // Preferences already set
			Date logDate = new Date(log.getTimestamp());
			String date = new SimpleDateFormat("yyyy-MM-dd").format(logDate);
			String hour = new SimpleDateFormat("hh:mm").format(logDate);
			url = new StringBuilder()
					.append(destUrl).append('/')
					.append(userId).append('/')
					.append(log.getLatitude()).append('/')
					.append(log.getLongitude()).append('/')
					.append(date).append('/')
					.append(hour).append('/')
					.append(log.getName())
					.toString();
		}
		return url;
	}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps);

        locationName = (EditText) findViewById(R.id.locationName);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationDao = new DbAdapter(this).getDiveLocationLogDao();
        locationDao.open();
    }

    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	locationDao.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_gps, menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
    	if (item.getItemId() == R.id.menu_settings) { // Settings
    		startActivity(new Intent(this, Preferences.class));
    		return true;
    	} else if (item.getItemId() == R.id.menu_locate) { // Locate has been clicked
    		final ProgressDialog waitDialog = ProgressDialog.show(GpsActivity.this, "", getString(R.string.wait_dialog), true);
			locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, new LocationListener() {
				
				@Override
				public void onStatusChanged(String provider, int status, Bundle extras) {
					waitDialog.dismiss();
				}

				@Override
				public void onProviderEnabled(String provider) {
					waitDialog.dismiss();
				}

				@Override
				public void onProviderDisabled(String provider) {
					waitDialog.dismiss();
					Toast.makeText(GpsActivity.this, R.string.error_location, Toast.LENGTH_SHORT).show();
				}
				
				@Override
				public void onLocationChanged(final Location location) {
					waitDialog.dismiss();
					final DiveLocationLog locationLog = new DiveLocationLog(location, locationName.getText().toString(), System.currentTimeMillis());
					Toast.makeText(GpsActivity.this, getString(R.string.confirmation_location_picked, locationLog.getName()), Toast.LENGTH_SHORT).show();
					new Thread(new Runnable() {
						public void run() {
							String url = getSendUrl(locationLog);
							try {
								new DefaultHttpClient().execute(new HttpGet(url));
								locationDao.deleteDiveLocationLog(locationLog);
							} catch (Exception e) {
								Log.d("GpsActivity", "Could not connect to " + url, e);
								locationDao.addDiveLocationLog(locationLog);
								runOnUiThread(new Runnable() {
									public void run() {
										Toast.makeText(GpsActivity.this, R.string.error_send, Toast.LENGTH_SHORT).show();
									}
								});
							}
						}
					}).start();
				}
			}, null);
    	} else if (item.getItemId() == R.id.menu_send) { // Send has been clicked
    		// TODO Progress
			new Thread(new Runnable() {
				public void run() {
					int success = 0;
					boolean urlFailure = false;
					List<DiveLocationLog> locations = locationDao.getAllDiveLocationLogs();
					for (DiveLocationLog log : locations) {
						String url = getSendUrl(log);
						if (url == null) { // Could not build URL, show error
							runOnUiThread(new Runnable() {
								public void run() {
									Toast.makeText(GpsActivity.this, R.string.error_no_settings, Toast.LENGTH_SHORT).show();
								}
							});
							urlFailure = true;
							break;
						} else {
							try {
								new DefaultHttpClient().execute(new HttpGet(url));
								locationDao.deleteDiveLocationLog(log);
								++success;
							} catch (Exception e) {
								Log.d("GpsActivity", "Could not connect to " + url, e);
							}
						}
					}
					if (!urlFailure) {
						final int successCount = success;
						final int totalCount = locations.size();
						runOnUiThread(new Runnable() {
							public void run() {
								Toast.makeText(GpsActivity.this, getString(R.string.confirmation_location_sent, successCount, totalCount), Toast.LENGTH_SHORT).show();
							}
						});
					}
				}
			}).start();
    	}
    	return super.onMenuItemSelected(featureId, item);
    }
}
