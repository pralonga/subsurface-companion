package org.subsurface;

import java.util.Date;
import java.util.List;

import org.subsurface.controller.DiveController;
import org.subsurface.model.DiveLocationLog;
import org.subsurface.ui.DiveDetailFragment;
import org.subsurface.util.DateUtils;
import org.subsurface.util.GpsUtil;
import org.subsurface.ws.WsException;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

/**
 * Activity for dive details.
 * @author Aurelien PRALONG
 *
 */
public class DiveDetailActivity extends SherlockFragmentActivity implements com.actionbarsherlock.view.ActionMode.Callback {

	private static final String TAG = DiveDetailActivity.class.getName();
	public static final String PARAM_DIVE_POSITION = "PARAM_DIVE_POSITION";
	public static final String PARAM_DIVE_SEARCH_START = "PARAM_DIVE_SEARCH_START";
	public static final String PARAM_DIVE_SEARCH_END = "PARAM_DIVE_SEARCH_END";
	public static final String PARAM_DIVE_SEARCH_NAME = "PARAM_DIVE_SEARCH_NAME";

	private ViewPager pager;
	private PagerAdapter pagerAdapter;
	private DiveLocationLog dive = null;
	private int divePosition = 0;
	private ActionMode actionMode = null;
	private TextView positionText = null;
	private ImageButton previous = null;
	private ImageButton next = null;
	private List<DiveLocationLog> dives = null;

	private void initView() {
		setContentView(R.layout.dive_detail_pager);
		this.positionText = (TextView) findViewById(R.id.currentPos);
		this.previous = (ImageButton) findViewById(R.id.previous);
		this.next = (ImageButton) findViewById(R.id.next);

		this.pager = (ViewPager) findViewById(R.id.pager);
		this.pagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
		pager.setAdapter(pagerAdapter);
		pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int position) {
				dive = dives.get(position);
				divePosition = position;
				positionText.setText((position + 1) + " / " + pagerAdapter.getCount());
				previous.setEnabled(position > 0);
				next.setEnabled(position < pagerAdapter.getCount() - 1);
			}
			
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }
			
			@Override
			public void onPageScrollStateChanged(int state) { }
		});
		positionText.setText((divePosition + 1) + " / " + pagerAdapter.getCount());
		pager.setCurrentItem(divePosition, false);

		previous.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				pager.setCurrentItem(divePosition > 0 ? divePosition - 1 : 0, false);
			}
		});
		
		next.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				pager.setCurrentItem(divePosition < pagerAdapter.getCount() ? divePosition + 1 : pagerAdapter.getCount() - 1, false);
			}
		});
		previous.setEnabled(divePosition > 0);
		next.setEnabled(divePosition < pagerAdapter.getCount() - 1);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		this.divePosition = getIntent().getIntExtra(PARAM_DIVE_POSITION, 0);
		long searchStart = getIntent().getLongExtra(PARAM_DIVE_SEARCH_START, 0);
		long searchEnd = getIntent().getLongExtra(PARAM_DIVE_SEARCH_END, 0);
		CharSequence searchName = getIntent().getCharSequenceExtra(PARAM_DIVE_SEARCH_NAME);
		if (searchStart > 0 || searchEnd > 0 || searchName != null) {
			this.dives = DiveController.instance.getFilteredDives(searchName == null ? null : searchName.toString(), searchStart, searchEnd, false);
		} else {
			this.dives = DiveController.instance.getDiveLogs();
		}
		this.dive = dives.get(divePosition);
		initView();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (dive != null) {
			getSupportMenuInflater().inflate(R.menu.dive_details, menu);
			if (dive.isSent()) {
				menu.findItem(R.id.menu_send).setVisible(false);
			}
		}
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
		if (item.getItemId() == android.R.id.home) {
			finish();
			return true;
		} else if (item.getItemId() == R.id.menu_map) {
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
							Toast.makeText(DiveDetailActivity.this, message, Toast.LENGTH_SHORT).show();
						}
					});
				}
			}).start();
		} else if (item.getItemId() == R.id.menu_delete) {
			new AlertDialog.Builder(this)
					.setTitle(R.string.menu_delete)
					.setMessage(R.string.confirm_delete_dive)
					.setNegativeButton(android.R.string.cancel, null)
					.setCancelable(true)
					.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							new Thread(new Runnable() {
								public void run() {
									int messageCode = R.string.error_delete_dive;
									try {
										DiveController.instance.deleteDiveLog(dive);
										messageCode = -1;
									} catch (WsException e) {
										messageCode = e.getCode();
									} catch (Exception e) {
										Log.d(TAG, "Could not delete dive", e);
									}
									final String message = messageCode == -1 ? null : getString(messageCode);
									runOnUiThread(new Runnable() {
										public void run() {
											if (message != null) {
												Toast.makeText(DiveDetailActivity.this, message, Toast.LENGTH_SHORT).show();
											} else {
												DiveDetailActivity.this.finish();
											}
										}
									});
								}
							}).start();
						}
					}).create().show();
		} else if (item.getItemId() == R.id.menu_edit) {
			this.actionMode = startActionMode(DiveDetailActivity.this);
		} else if (item.getItemId() == R.id.menu_settings) { // Settings
    		startActivity(new Intent(this, Preferences.class));
    		return true;
    	}
		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		mode.setTitle(getString(R.string.details_action_mode_title));
		getSupportMenuInflater().inflate(R.menu.dive_details_edit, menu);
		setContentView(R.layout.dive_detail_edit);
		((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
		((EditText) findViewById(R.id.title)).setText(dive.getName());
		((TextView) findViewById(R.id.date)).setText(
				DateUtils.initGMT(getString(R.string.date_format_full)).format(new Date(dive.getTimestamp())));
		((TextView) findViewById(R.id.coordinates)).setText(
				GpsUtil.buildCoordinatesString(this, dive.getLatitude(), dive.getLongitude()));
		return true;
	}

	@Override
	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		return false;
	}

	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		if (item.getItemId() == R.id.menu_save) {
			dive.setName(((EditText) findViewById(R.id.title)).getText().toString());
			DiveController.instance.updateDiveLog(dive);
		}
		actionMode.finish();
		return true;
	}

	@Override
	public void onDestroyActionMode(ActionMode mode) {
		initView();
		actionMode = null;
	}

	/**
	 * {@link FragmentStatePagerAdapter} for dives.
	 * @author Aurelien PRALONG
	 *
	 */
	private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
		public ScreenSlidePagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public android.support.v4.app.Fragment getItem(int position) {
			Fragment fragment = new DiveDetailFragment();
			Bundle args = new Bundle();
			args.putLong(DiveDetailFragment.PARAM_DIVE_ID, dives.get(position).getId());
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public int getCount() {
			return dives.size();
		}
	}

	@Override
	public void finish() {
		super.finish();
		// Disable out animation
		overridePendingTransition(0, 0);
	}
}
