package org.subsurface;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.subsurface.controller.DiveController;
import org.subsurface.controller.UserController;
import org.subsurface.fragment.DiveListFragment;
import org.subsurface.fragment.DiveMapFragment;
import org.subsurface.fragment.DiveReceiver;
import org.subsurface.model.DiveLocationLog;
import org.subsurface.util.DateUtils;
import org.subsurface.ws.WsException;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.v4.app.ActivityCompat;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class DiveListActivity extends SherlockFragmentActivity implements OnNavigationListener {

	private static final String TAG = "DiveListActivity";

	private static final int PICK_MAP_REQCODE = 999;
	private static final int PICK_GPXFILE_REQCODE = 998;
	private static final String GPX_DIVE_LOGS  = "gpxdivelogs";
	private static final String MAP_DIVE_LOG  = "mapdivelog";

	private IBinder service = null;
	private final ServiceConnection connection = new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			service = null;
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			DiveListActivity.this.service = service;
			try {
				Message m = Message.obtain(null, BackgroundLocationService.WHAT_REGISTER_LISTENER);
				m.replyTo = new Messenger(serviceHandler);
				new Messenger(service).send(m);
			} catch (Exception e) {
				Log.d(TAG, "Could not register listener", e);
			}
		}
	};

	private final Handler serviceHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			default:
				DiveController.instance.forceUpdate();
				currentFragment.onRefreshDives();
				break;
			}
		}
	};

	private LocationManager locationManager = null;
	private MenuItem refreshItem = null;
	private ActionMode actionMode;
	private LocationListener locationListener = null;

	// Search
	private View dateFilterLayout;

	// Fragments
	private DiveMapFragment mapsFragment = new DiveMapFragment();
	private DiveListFragment diveListFragment = new DiveListFragment();
	private DiveReceiver currentFragment = null;

	private void showGpsWarning() {
		new AlertDialog.Builder(this)
				.setTitle(R.string.confirm_enable_gps_title)
				.setMessage(R.string.confirm_enable_gps)
				.setCancelable(true)
				.setNegativeButton(android.R.string.no, null)
				.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
					}
				}).create().show();
	}

	private boolean isBackgroundLocationServiceStarted() {
		boolean started = false;
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if (BackgroundLocationService.class.getName().equals(service.service.getClassName())) {
	            started = true;
	            break;
	        }
	    }
	    return started;
	}

	private void refresh() {
		if (refreshItem != null) {
			refreshItem.setActionView(R.layout.refresh);
			new AsyncTask<Void, Void, Integer>() {
				@Override
				protected Integer doInBackground(Void... params) {
					Integer message = R.string.error_generic;
					try {
						DiveController.instance.startUpdate();
						message = R.string.success_refresh;
					} catch (WsException e) {
						message = e.getCode();
					} catch (Exception e) {
						Log.d(TAG, "Could not complete update", e);
					}
					return message;
				}
				@Override
				protected void onPostExecute(Integer success) {
					currentFragment.onRefreshDives();
					Toast.makeText(DiveListActivity.this, success, Toast.LENGTH_SHORT).show();
					refreshItem.setActionView(null);
				}
			}.execute();
		}
	}

	public void sendDives(final List<DiveLocationLog> dives) {
		// Should be get in a thread, but ProgressDialog does not allow post-show modifications...
		final ProgressDialog dialog = new ProgressDialog(this);
		final AtomicBoolean cancel = new AtomicBoolean(false);
		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		dialog.setMax(dives.size());
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
			Toast.makeText(this, R.string.error_no_settings, Toast.LENGTH_SHORT).show();
		} else { // Send locations
			final Handler handler = new Handler() {
    			@Override
    			public void handleMessage(Message msg) {
    				int total = msg.arg1;
    				dialog.setProgress(total);
    				if (total >= dives.size()) { // OK, close dialog
    					dialog.dismiss();
    				}
    			}
    		};
			new Thread(new Runnable() {
				public void run() {
					int success = 0;
					for (int i = 0; i < dives.size() && !cancel.get(); ++i) {
						DiveLocationLog log = dives.get(i);
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
					msg.arg1 = dives.size();
					handler.sendMessage(msg);

					final int successCount = success;
					final int totalCount = dives.size();
					runOnUiThread(new Runnable() {
						public void run() {
							currentFragment.onRefreshDives();
							Toast.makeText(DiveListActivity.this, getString(R.string.confirmation_locations_sent, successCount, totalCount), Toast.LENGTH_SHORT).show();
						}
					});
				}
			}).start();
		}
	}

	/**
	 * Send location picked from map to the server and update the list
	 * @param divelog DiveLocationLog of the dive
	 */
	public void sendMapDiveLog(DiveLocationLog divelog) {
		if (UserController.instance.autoSend()) {
			try {
				DiveController.instance.sendDiveLog(divelog);
				Toast.makeText(DiveListActivity.this, getString(R.string.confirmation_dive_picked_sent, divelog.getName()), Toast.LENGTH_SHORT).show();
			} catch (final WsException e) {
				runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(DiveListActivity.this, e.getCode(), Toast.LENGTH_SHORT).show();
					}
				});
			} catch (Exception e) {
				Log.d(TAG, "Could not send dive " + divelog.getName(), e);
				runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(DiveListActivity.this, R.string.error_send, Toast.LENGTH_SHORT).show();
					}
				});
			}
		} else {
			DiveController.instance.updateDiveLog(divelog);
			Toast.makeText(DiveListActivity.this, getString(R.string.confirmation_location_picked, divelog.getName()), Toast.LENGTH_SHORT).show();
		}
		runOnUiThread(new Runnable() {
			public void run() {
				currentFragment.onRefreshDives();
			}
		});
	}

	private void sendDiveLog(String name) {
		final DiveLocationLog locationLog = new DiveLocationLog();
		locationLog.setName(name);
		final AtomicBoolean cancel = new AtomicBoolean(false);
		final ProgressDialog waitDialog = ProgressDialog.show(
				DiveListActivity.this,
				"", getString(R.string.dialog_wait),
				true, true, new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						cancel.set(true);
						if (locationListener != null) {
							locationManager.removeUpdates(locationListener);
							locationListener = null;
						}
						Log.d(TAG, "Location cancelled");
					}
				});
		this.locationListener = new LocationListener() {
			
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
					Toast.makeText(DiveListActivity.this, R.string.error_location, Toast.LENGTH_SHORT).show();
				}
			}
			
			@Override
			public void onLocationChanged(final Location location) {
				if (!cancel.get()) {
					waitDialog.dismiss();
					Toast.makeText(DiveListActivity.this, getString(R.string.confirmation_location_picked, locationLog.getName()), Toast.LENGTH_SHORT).show();
					new Thread(new Runnable() {
						public void run() {
							locationLog.setLocation(location);
							locationLog.setTimestamp(DateUtils.getFakeUtcDate());
							if (UserController.instance.autoSend()) {
								try {
									DiveController.instance.sendDiveLog(locationLog);
								} catch (final WsException e) {
									runOnUiThread(new Runnable() {
										public void run() {
											Toast.makeText(DiveListActivity.this, e.getCode(), Toast.LENGTH_SHORT).show();
										}
									});
								} catch (Exception e) {
									Log.d(TAG, "Could not send dive " + locationLog.getName(), e);
									runOnUiThread(new Runnable() {
										public void run() {
											Toast.makeText(DiveListActivity.this, R.string.error_send, Toast.LENGTH_SHORT).show();
										}
									});
								}
							} else {
								DiveController.instance.updateDiveLog(locationLog);
							}
							runOnUiThread(new Runnable() {
								public void run() {
									currentFragment.onRefreshDives();
								}
							});
						}
					}).start();
				}
			}
		};
		locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, null);
	}

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);

		UserController.instance.setContext(this);
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

        ArrayAdapter<CharSequence> listAdapter = ArrayAdapter.createFromResource(
				this, R.array.list_menu_choices,
				R.layout.spinner_item);
		listAdapter.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);
		getSupportActionBar().setTitle(null);
		getSupportActionBar().setListNavigationCallbacks(listAdapter, this);
        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        // Retrieve location service
        this.locationManager  = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    	setContentView(R.layout.main_view);

    	// Register for dive service updates
    	if (isBackgroundLocationServiceStarted()) {
    		bindService(new Intent(this, BackgroundLocationService.class), connection, Context.BIND_NOT_FOREGROUND);
    	}

    	// Hide search
    	this.dateFilterLayout = findViewById(R.id.dateFilterLayout);
    	dateFilterLayout.setVisibility(View.GONE);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		if (UserController.instance.syncOnstartup()) {
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					refresh();
				}
			}, 1000);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (service != null) { // Unbind service
			try {
				Message m = Message.obtain(null, BackgroundLocationService.WHAT_UNREGISTER_LISTENER);
				m.replyTo = new Messenger(serviceHandler);
				new Messenger(service).send(m);
			} catch (Exception e) {
				Log.d(TAG, "Could not unbind service", e);
			}
			unbindService(connection);
		}
	}

	@Override
	public boolean onSearchRequested() {
		dateFilterLayout.setVisibility(View.VISIBLE);
		// TODO Handle search via this activity, not a separate activity
		startActivity(new Intent(this, SearchDiveActivity.class));
		return true;
	}

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		boolean handled = false;
		if (itemPosition == 0) {
			getSupportFragmentManager().beginTransaction().replace(R.id.divePart, diveListFragment).commit();
			currentFragment = diveListFragment;
			handled = true;
		} else if (itemPosition == 1) {
			getSupportFragmentManager().beginTransaction().replace(R.id.divePart, mapsFragment).commit();
			currentFragment = mapsFragment;
			handled = true;
		}
		return handled;
	}

	@Override
	public void onActivityResult(int requestcode, int resultCode, Intent data) {
		if(resultCode == RESULT_OK && data != null) {
			Bundle rec_bundle = data.getExtras();
			switch(requestcode) {
			case PICK_MAP_REQCODE:
				DiveLocationLog mapdivelog = (DiveLocationLog) rec_bundle.get(MAP_DIVE_LOG);
				sendMapDiveLog(mapdivelog);
				return;
			case PICK_GPXFILE_REQCODE:
				List<DiveLocationLog> gpxdivelogs = (ArrayList<DiveLocationLog>) rec_bundle.get(GPX_DIVE_LOGS);
				sendDives(gpxdivelogs);
				return;
			}
		} else { // either some error has occurred or no data has been received
			Toast.makeText(this, R.string.error_no_dive_found, Toast.LENGTH_SHORT).show();
		}
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.main, menu);
		refreshItem = menu.findItem(R.id.menu_refresh);
		if (isBackgroundLocationServiceStarted()) {
			menu.findItem(R.id.menu_start_background_service).setTitle(getString(R.string.menu_stop_background_service));
		} else {
			menu.findItem(R.id.menu_start_background_service).setTitle(getString(R.string.menu_start_background_service));
		}
        return true;
    }

	@Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
		boolean handled = false;

		switch (item.getItemId()) {
		case R.id.menu_settings:
			startActivity(new Intent(this, Preferences.class));
			handled = true;
			break;
		case R.id.menu_new_map:
			startActivityForResult(new Intent(this, PickLocationMap.class), PICK_MAP_REQCODE);
			handled = true;
			break;
		case R.id.menu_new_current:
			if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
    			final EditText edit = new EditText(this);
    			AlertDialog.Builder builder = new AlertDialog.Builder(this);
    			builder.setView(edit);
    			builder.setNegativeButton(android.R.string.cancel, null);
    			edit.setHint(getString(R.string.hint_dive_name));
    			edit.setInputType(InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE);
    			builder.setTitle(getString(R.string.dialog_location_name))
    					.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
    						@Override
    						public void onClick(DialogInterface dialog, int which) {
    							sendDiveLog(edit.getText().toString());
    						}
    					}).create().show();
    		} else {
    			showGpsWarning();
    		}
			handled = true;
			break;
		case R.id.menu_new_import:
			startActivityForResult(new Intent(this, PickGpx.class), PICK_GPXFILE_REQCODE);
			handled = true;
			break;
		case R.id.menu_logoff:
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
				    		startActivity(new Intent(DiveListActivity.this, AccountLinkActivity.class));
				    		finish();
						}
					}).create().show();
			handled = true;
			break;
		case R.id.menu_refresh:
			refresh();
			handled = true;
			break;
		case R.id.menu_search:
			handled = onSearchRequested();
			break;
		case R.id.menu_start_background_service:
			if (isBackgroundLocationServiceStarted()) { // Stop service
    			if (!stopService(new Intent(this, BackgroundLocationService.class))) {
    				Toast.makeText(this, R.string.error_background_service_unstoppable, Toast.LENGTH_SHORT).show();
    			}
    		} else { // Start service
    			if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
    				startService(new Intent(this, BackgroundLocationService.class));
        			bindService(new Intent(this, BackgroundLocationService.class), connection, Context.BIND_NOT_FOREGROUND);
        		} else {
        			showGpsWarning();
        		}
    		}
    		ActivityCompat.invalidateOptionsMenu(this);
    		handled = true;
			break;
		default:
			break;
		}

    	return handled || super.onMenuItemSelected(featureId, item);
	}
}
