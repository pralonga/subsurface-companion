package org.subsurface.fragment;

import java.util.ArrayList;
import java.util.List;

import org.subsurface.DiveDetailActivity;
import org.subsurface.DiveListActivity;
import org.subsurface.R;
import org.subsurface.controller.DiveController;
import org.subsurface.model.DiveLocationLog;
import org.subsurface.ui.DiveArrayAdapter;
import org.subsurface.ui.DiveArrayAdapter.SelectionListener;
import org.subsurface.ws.WsException;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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

public class DiveListFragment extends SherlockListFragment implements com.actionbarsherlock.view.ActionMode.Callback, SelectionListener, DiveReceiver {

	private static final String TAG = "DiveListFragment";

	private ActionMode actionMode;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View result = inflater.inflate(R.layout.dive_list, null);
		DiveArrayAdapter adapter = new DiveArrayAdapter(getActivity());
    	adapter.setListener(this);
    	setListAdapter(adapter);
    	//getListView().setItemsCanFocus(true);
		return result;
	}

	@Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.dives, menu);
    }

	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
		boolean handled = false;

    	switch (item.getItemId()) {
		case R.id.menu_send_all:
			((DiveListActivity) getActivity()).sendDives(DiveController.instance.getPendingLogs());
			handled = true;
			break;
		default:
			break;
		}

    	return handled || super.onOptionsItemSelected(item);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Intent detailIntent = new Intent(getActivity(), DiveDetailActivity.class);
		detailIntent.putExtra(DiveDetailActivity.PARAM_DIVE_POSITION, position);
		startActivity(detailIntent);
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
		switch (item.getItemId()) {
		case R.id.menu_send:
			ArrayList<DiveLocationLog> copy = new ArrayList<DiveLocationLog>();
			for (DiveLocationLog log : dives) {
				if (!log.isSent()) {
					copy.add(log);
				}
			}
			((DiveListActivity) getActivity()).sendDives(copy);
			break;
		case R.id.menu_delete:
			if (!dives.isEmpty()) {
				new AlertDialog.Builder(getActivity())
					.setTitle(R.string.menu_delete)
					.setMessage(R.string.confirm_delete_dives)
					.setNegativeButton(android.R.string.cancel, null)
					.setCancelable(true)
					.setPositiveButton(android.R.string.ok,	new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,	int which) {
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
			break;
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

	@Override
	public void onRefreshDives() {
		((DiveArrayAdapter) getListAdapter()).notifyDataSetChanged();
	}
}
