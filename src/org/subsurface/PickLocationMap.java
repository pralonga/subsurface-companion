package org.subsurface;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.subsurface.model.DiveLocationLog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.NavUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
/**
 * Activity to choose the dive location on the map
 * @author Venkatesh Shukla
 */
public class PickLocationMap extends SherlockFragmentActivity implements OnMapLongClickListener, OnMarkerClickListener {
	private GoogleMap mMap;
	private static LatLng latlng;
	private static final String MAP_DIVE_LOG  = "mapdivelog";
	private MarkerOptions markeropt;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle(R.string.title_picklocationmap);
		setContentView(R.layout.dive_picklocation);
		setUpMapIfNeeded();
		markeropt = new MarkerOptions()
			.title(getString(R.string.title_map_marker))
			.draggable(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.picklocation_map, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		case R.id.dive_loc_finish:
			showDialog();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	void showDialog() {
	    DialogFragment newFragment = new MyAlertDialogFragment();
	    newFragment.setShowsDialog(true);
	    newFragment.show(getSupportFragmentManager(), "dialog");
	}

	public void doPositiveClick(long timestamp, String divename) {
		DiveLocationLog divelog = new DiveLocationLog();
		divelog.setLatitude(latlng.latitude);
		divelog.setLongitude(latlng.longitude);
		divelog.setName(divename);
		divelog.setTimestamp(timestamp);
		divelog.setSent(false);

		Intent resultIntent = new Intent();
		resultIntent.putExtra(MAP_DIVE_LOG, divelog);
		setResult(Activity.RESULT_OK, resultIntent);
		finish();
	}

	@Override
	protected void onResume() {
		super.onResume();
		setUpMapIfNeeded();
	}

	private void setUpMapIfNeeded() {
		if (mMap == null) {
			mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
			if (mMap != null) {
				mMap.setOnMapLongClickListener(this);
			}
		}
	}

	@Override
	public void onMapLongClick(LatLng loc) {
		mMap.clear();
		markeropt.position(loc);
		markeropt.snippet(String.format("(%f, %f)", loc.latitude, loc.longitude));
		mMap.addMarker(markeropt);
		latlng = loc;
	}

	@Override
	public boolean onMarkerClick(Marker arg0) {
		return false;
	}

	public static class MyAlertDialogFragment extends DialogFragment {
		private EditText etDiveName;
		private DatePicker datePickerMap;
		private TimePicker timePickerMap;
		private View dialogView;
		private int hour, minute, day, month, year;
		private String divename;
		private long timestamp;
	    @Override
	    public Dialog onCreateDialog(Bundle savedInstanceState) {
		dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.datetimedialog, null);
		etDiveName = (EditText) dialogView.findViewById(R.id.etMapDiveName);
		datePickerMap = (DatePicker) dialogView.findViewById(R.id.datePickerMap);
		timePickerMap = (TimePicker) dialogView.findViewById(R.id.timePickerMap);

	        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	        builder.setIcon(R.drawable.ic_menu_map);
	        builder.setTitle(R.string.title_map_marker);
	        builder.setView(dialogView);
	        builder.setNegativeButton(android.R.string.cancel, null);
	        builder.setPositiveButton(android.R.string.ok,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
				divename = etDiveName.getText().toString();
				if(divename.contentEquals("")) divename = getString(R.string.default_map_dive_name);
				minute = timePickerMap.getCurrentMinute();
				hour = timePickerMap.getCurrentHour();
				year = datePickerMap.getYear();
				month = datePickerMap.getMonth();
				day = datePickerMap.getDayOfMonth();
				Calendar cal = new GregorianCalendar(year, month, day, hour, minute);
				cal.setTimeZone(TimeZone.getTimeZone("GMT"));
				timestamp = cal.getTime().getTime();
				((PickLocationMap) getActivity()).doPositiveClick(timestamp, divename);
                        }
                    }
                );
            return builder.create();
	    }
	}
}
