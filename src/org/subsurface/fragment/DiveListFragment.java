package org.subsurface.fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.subsurface.DiveDetailActivity;
import org.subsurface.R;
import org.subsurface.controller.DiveController;
import org.subsurface.controller.UserController;
import org.subsurface.model.DiveLocationLog;
import org.subsurface.ui.DiveArrayAdapter;
import org.subsurface.ui.DiveArrayAdapter.SelectionListener;
import org.subsurface.ws.WsException;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class DiveListFragment extends SherlockListFragment implements com.actionbarsherlock.view.ActionMode.Callback, SelectionListener {

	private static final String TAG = "DiveListFragment";

	private MenuItem refreshItem = null;
	private ActionMode actionMode;

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
					((DiveArrayAdapter) getListAdapter()).notifyDataSetChanged();
					Toast.makeText(getActivity(), success, Toast.LENGTH_SHORT).show();
					refreshItem.setActionView(null);
				}
			}.execute();
		}
	}

	private void sendDives(final List<DiveLocationLog> dives) {
		// Should be get in a thread, but ProgressDialog does not allow post-show modifications...
		final ProgressDialog dialog = new ProgressDialog(getActivity());
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
			Toast.makeText(getActivity(), R.string.error_no_settings, Toast.LENGTH_SHORT).show();
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
					getActivity().runOnUiThread(new Runnable() {
						public void run() {
							((DiveArrayAdapter) getListAdapter()).notifyDataSetChanged();
							Toast.makeText(getActivity(), getString(R.string.confirmation_locations_sent, successCount, totalCount), Toast.LENGTH_SHORT).show();
						}
					});
				}
			}).start();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View result = inflater.inflate(R.layout.dive_list, null);
		DiveArrayAdapter adapter = new DiveArrayAdapter(getActivity());
    	adapter.setListener(this);
    	setListAdapter(adapter);
    	getListView().setItemsCanFocus(true);
		return result;
	}

	@Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.dives, menu);
		refreshItem = menu.findItem(R.id.menu_refresh);
    }

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Intent detailIntent = new Intent(getActivity(), DiveDetailActivity.class);
		detailIntent.putExtra(DiveDetailActivity.PARAM_DIVE_POSITION, position);
		startActivity(detailIntent);
	}

	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	if (item.getItemId() == R.id.menu_refresh) {
    		refresh();
    		return true;
    	}
    	return super.onOptionsItemSelected(item);
	}

	@Override
	public void onSelectedItemsChanged(List<DiveLocationLog> dives) {
		if (dives.size() > 0) {
			if (actionMode == null) {
				actionMode = ((SherlockFragmentActivity) getActivity()).startActionMode(DiveListFragment.this);
			}
			actionMode.setTitle(getString(R.string.home_action_mode_title, dives.size()));
		} else {
			if (actionMode != null) {
				actionMode.finish();
			}
		}
	}

	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		((SherlockFragmentActivity) getActivity()).getSupportMenuInflater().inflate(R.menu.action_mode_dives, menu);
		return true;
	}

	@Override
	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		return false;
	}

	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		final List<DiveLocationLog> dives = ((DiveArrayAdapter) getListAdapter()).getSelectedDives();
		if (item.getItemId() == R.id.menu_send) {
			ArrayList<DiveLocationLog> copy = new ArrayList<DiveLocationLog>();
			for (DiveLocationLog log : dives) {
				if (!log.isSent()) {
					copy.add(log);
				}
			}
			sendDives(copy);
		} else if (item.getItemId() == R.id.menu_delete) {
			if (!dives.isEmpty()) {
				new AlertDialog.Builder(getActivity())
					.setTitle(R.string.menu_delete)
					.setMessage(R.string.confirm_delete_dives)
					.setNegativeButton(android.R.string.cancel, null)
					.setCancelable(true)
					.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							new Thread(new Runnable() {
								public void run() {
									int messageCode = R.string.error_delete_dives;
									try {
										for (DiveLocationLog log : dives) {
											DiveController.instance.deleteDiveLog(log);
										}
										messageCode = -1;
									} catch (WsException e) {
										messageCode = e.getCode();
									} catch (Exception e) {
										Log.d(TAG, "Could not delete dives", e);
									}
									final String message = messageCode == -1 ? null : getString(messageCode);
									getActivity().runOnUiThread(new Runnable() {
										public void run() {
											((DiveArrayAdapter) getListAdapter()).notifyDataSetChanged();
											if (message != null) {
												Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
											}
										}
									});
								}
							}).start();
						}
					}).create().show();
			}
		}
		actionMode.finish();
		return true;
	}

	@Override
	public void onDestroyActionMode(ActionMode mode) {
		((DiveArrayAdapter) getListAdapter()).unselectAll();
		((DiveArrayAdapter) getListAdapter()).notifyDataSetChanged();
		actionMode = null;
	}
}
