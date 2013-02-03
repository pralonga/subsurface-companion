package org.subsurface;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.subsurface.controller.DiveController;
import org.subsurface.controller.UserController;
import org.subsurface.model.DiveLocationLog;
import org.subsurface.ui.DiveArrayAdapter;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;

public class HomeActivity extends SherlockListActivity implements com.actionbarsherlock.view.ActionMode.Callback {

	private static final String TAG = "HomeActivity";
	private LocationManager locationManager = null;
	private MenuItem refreshItem = null;
	private ActionMode actionMode;

	public void refresh() {
		refreshItem.setVisible(false);
		setSupportProgressBarIndeterminateVisibility(true);
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
				refreshItem.setVisible(true);
				setSupportProgressBarIndeterminateVisibility(false);
			}
		}.execute();
	}

	private void sendDives(final List<DiveLocationLog> dives) {
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
			Toast.makeText(HomeActivity.this, R.string.error_no_settings, Toast.LENGTH_SHORT).show();
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
							Toast.makeText(HomeActivity.this, getString(R.string.confirmation_location_sent, successCount, totalCount), Toast.LENGTH_SHORT).show();
						}
					});
				}
			}).start();
		}
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

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        // Retrieve location service
        this.locationManager  = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    	setContentView(R.layout.dive_list);
    	setListAdapter(new DiveArrayAdapter(this));
    	getListView().setItemsCanFocus(false);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		setSupportProgressBarIndeterminateVisibility(false);
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.dives, menu);
		refreshItem = menu.findItem(R.id.menu_refresh);
        return true;
    }

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		SparseBooleanArray checked = getListView().getCheckedItemPositions();
		int checkedElementCount = 0;
		for (int i = 0; i < checked.size(); ++i) {
			if (checked.valueAt(i)) {
				++checkedElementCount;
			}
		}

		if (checkedElementCount > 0) {
			if (actionMode == null) {
				actionMode = startActionMode(HomeActivity.this);
			}
			actionMode.setTitle(getString(R.string.action_mode_title, checkedElementCount));
		} else {
			if (actionMode != null) {
				actionMode.finish();
			}
		}
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
    	} else if (item.getItemId() == R.id.menu_send_all) { // Send has been clicked
    		sendDives(DiveController.instance.getPendingLogs());
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

	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		getSupportMenuInflater().inflate(R.menu.action_mode_dives, menu);
		return true;
	}

	@Override
	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		return false;
	}

	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		DiveArrayAdapter adapter = (DiveArrayAdapter) getListView().getAdapter();
		SparseBooleanArray checked = getListView().getCheckedItemPositions();
		ArrayList<DiveLocationLog> dives = new ArrayList<DiveLocationLog>();
		for (int i = 0; i < checked.size(); ++i) {
			if (checked.valueAt(i)) {
				dives.add(adapter.getItem(i));
			}
		}
		if (item.getItemId() == R.id.menu_map) {
			startActivity(new Intent(
					Intent.ACTION_VIEW,
					Uri.parse("geo:" + dives.get(0).getLatitude() + "," + dives.get(0).getLongitude())
					));
		} else if (item.getItemId() == R.id.menu_send) {
			
		} else if (item.getItemId() == R.id.menu_delete) {
			
		}
		return false;
	}

	@Override
	public void onDestroyActionMode(ActionMode mode) {
		for (int i = 0; i < getListView().getAdapter().getCount(); ++i) {
			getListView().setItemChecked(i, false);
		}
		actionMode = null;
	}
}
