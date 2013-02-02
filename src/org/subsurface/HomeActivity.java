package org.subsurface;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.subsurface.controller.DiveController;
import org.subsurface.controller.UserController;
import org.subsurface.model.DiveLocationLog;
import org.subsurface.ui.DiveArrayAdapter;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class HomeActivity extends ListActivity {

	private static final String TAG = "HomeActivity";
	private LocationManager locationManager = null;
	private MenuItem refreshItem = null;

	public void refresh() {
		refreshItem.setVisible(false);
		// TODO progress update
		new AsyncTask<Void, Void, Boolean>() {
			@Override
			protected Boolean doInBackground(Void... params) {
				Boolean success = false;
				try {
					DiveController.instance.startUpdate();
					success = true;
				} catch (Exception e) {
					Log.d(TAG, "Could not complete update", e);
				}
				return success;
			}
			@Override
			protected void onPostExecute(Boolean success) {
				((DiveArrayAdapter) getListAdapter()).notifyDataSetChanged();
				Toast.makeText(HomeActivity.this, success ? R.string.success_refresh : R.string.error_generic, Toast.LENGTH_SHORT).show();
			}
		}.execute();
		
		refreshItem.setVisible(true);
		// TODO Stop progress
	}

	private void sendDiveLog(String name) {
		final DiveLocationLog locationLog = new DiveLocationLog();
		locationLog.setName(name);
		final AtomicBoolean cancel = new AtomicBoolean(false);
		final ProgressDialog waitDialog = ProgressDialog.show(
				HomeActivity.this,
				"", getString(R.string.wait_dialog),
				true, true, new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						cancel.set(true);
						Log.d(TAG, "Location cancelled");
					}
				});
		locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, new LocationListener() {
			
			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) {
				if (!cancel.get()) {
					waitDialog.dismiss();
				}
			}

			@Override
			public void onProviderEnabled(String provider) {
				if (!cancel.get()) {
					waitDialog.dismiss();
				}
			}

			@Override
			public void onProviderDisabled(String provider) {
				if (!cancel.get()) {
					waitDialog.dismiss();
					Toast.makeText(HomeActivity.this, R.string.error_location, Toast.LENGTH_SHORT).show();
				}
			}
			
			@Override
			public void onLocationChanged(final Location location) {
				if (!cancel.get()) {
					waitDialog.dismiss();
					Toast.makeText(HomeActivity.this, getString(R.string.confirmation_location_picked, locationLog.getName()), Toast.LENGTH_SHORT).show();
					new Thread(new Runnable() {
						public void run() {
							locationLog.setLocation(location);
							locationLog.setTimestamp(System.currentTimeMillis());
							try {
								DiveController.instance.sendDiveLog(locationLog);
							} catch (Exception e) {
								Log.d(TAG, "Could not send dive " + locationLog.getName(), e);
								runOnUiThread(new Runnable() {
									public void run() {
										Toast.makeText(HomeActivity.this, R.string.error_send, Toast.LENGTH_SHORT).show();
									}
								});
							}
						}
					}).start();
				}
			}
		}, null);
	}

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize controllers
        UserController.instance.setContext(this);
        DiveController.instance.setContext(this);

        // Retrieve location service
        this.locationManager  = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    	setContentView(R.layout.dive_list);
    	setListAdapter(new DiveArrayAdapter(this));
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.dives, menu);
		refreshItem = menu.findItem(R.id.menu_refresh);
        return true;
    }

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Show clicked dive
		
	}

	@Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
    	if (item.getItemId() == R.id.menu_settings) { // Settings
    		startActivity(new Intent(this, Preferences.class));
    		return true;
    	} else if (item.getItemId() == R.id.menu_new) { // Locate has been clicked
    		final EditText edit = new EditText(this);
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setView(edit);
			builder.setNegativeButton(android.R.string.cancel, null);
			edit.setHint(getString(R.string.hint_dive_name));
			edit.setInputType(InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE);
			builder.setTitle(getString(R.string.location_name))
					.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							sendDiveLog(edit.getText().toString());
						}
					}).create().show();
			return true;
    	} else if (item.getItemId() == R.id.menu_send) { // Send has been clicked
    		// Should be get in a thread, but ProgressDialog does not allow post-show modifications...
    		final List<DiveLocationLog> locations = DiveController.instance.getPendingLogs();
    		final ProgressDialog dialog = new ProgressDialog(this);
    		final AtomicBoolean cancel = new AtomicBoolean(false);
    		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			dialog.setMax(locations.size());
			dialog.setProgress(0);
    		dialog.setMessage(getString(R.string.dialog_wait_send));
    		dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					cancel.set(true);
				}
			});
    		dialog.show();
    		if (UserController.instance.getBaseUrl() == null) {
    			Toast.makeText(HomeActivity.this, R.string.error_no_settings, Toast.LENGTH_SHORT).show();
    		} else { // Send locations
    			final Handler handler = new Handler() {
        			@Override
        			public void handleMessage(Message msg) {
        				int total = msg.arg1;
        				dialog.setProgress(total);
        				if (total >= locations.size()) { // OK, close dialog
        					dialog.dismiss();
        				}
        			}
        		};
    			new Thread(new Runnable() {
    				public void run() {
    					int success = 0;
    					for (int i = 0; i < locations.size() && !cancel.get(); ++i) {
    						DiveLocationLog log = locations.get(i);
    						try {
    							DiveController.instance.sendDiveLog(log);
    							++success;
    						} catch (Exception e) {
    							Log.d(TAG, "Could not send dive", e);
    						}
    						// Update progress
    						Message msg = handler.obtainMessage();
    						msg.arg1 = i + 1;
    						handler.sendMessage(msg);
    					}

    					// 100 % 
    					Message msg = handler.obtainMessage();
    					msg.arg1 = locations.size();
    					handler.sendMessage(msg);

    					final int successCount = success;
    					final int totalCount = locations.size();
    					runOnUiThread(new Runnable() {
    						public void run() {
    							Toast.makeText(HomeActivity.this, getString(R.string.confirmation_location_sent, successCount, totalCount), Toast.LENGTH_SHORT).show();
    						}
    					});
    				}
    			}).start();
    		}
    		return true;
    	} else if (item.getItemId() == R.id.menu_logoff) {
    		new AlertDialog.Builder(this)
    				.setTitle(R.string.menu_logoff)
    				.setIcon(android.R.drawable.ic_dialog_alert)
    				.setMessage(R.string.confirm_disconnect)
    				.setNegativeButton(android.R.string.cancel, null)
    				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							DiveController.instance.deleteAll();
				    		UserController.instance.setUser(null);
				    		startActivity(new Intent(HomeActivity.this, AccountLinkActivity.class));
				    		finish();
						}
					}).create().show();
    		return true;
    	} else if (item.getItemId() == R.id.menu_refresh) {
    		refresh();
    	}
    	return super.onMenuItemSelected(featureId, item);
	}
}
