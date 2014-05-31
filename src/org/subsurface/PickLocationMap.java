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
import android.location.Location;
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
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Activity to choose the dive location on the map
 * @author Venkatesh Shukla
 */
public class PickLocationMap extends SherlockFragmentActivity
	implements OnMapLongClickListener, OnMarkerClickListener,
	ConnectionCallbacks, OnConnectionFailedListener, OnMyLocationButtonClickListener, LocationListener {

	private static final LocationRequest REQUEST = LocationRequest.create()
			.setInterval(5000)
			.setFastestInterval(16)
			.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
	public static final String MAP_DIVE_LOG  = "mapdivelog";

	private GoogleMap mMap;
	private LocationClient mLocationClient;
	private LatLng latlng;
	private MarkerOptions markeropt;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle(R.string.title_picklocationmap);
		setContentView(R.layout.dive_picklocation);
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
		if (latlng != null) {
			DialogFragment newFragment = new MyAlertDialogFragment();
		    newFragment.setShowsDialog(true);
		    newFragment.show(getSupportFragmentManager(), "dialog");
		} else {
			finish();
		}
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
		setUpMap();
		setUpLocationClient();
		mLocationClient.connect();
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mLocationClient != null) {
			mLocationClient.disconnect();
		}
	}

	private void setUpMap() {
		if (mMap == null) {
			mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
			if (mMap != null) {
				mMap.setOnMapLongClickListener(this);
				mMap.setOnMyLocationButtonClickListener(this);
				mMap.setMyLocationEnabled(true);
			}
		}
	}

	private void setUpLocationClient() {
        if (mLocationClient == null) {
            mLocationClient = new LocationClient( getApplicationContext(), this, this);
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

    @Override
    public void onConnected(Bundle connectionHint) {
        mLocationClient.requestLocationUpdates(REQUEST, this);
    }

    @Override
	public void onDisconnected() {}

   @Override
	public void onConnectionFailed(ConnectionResult arg0) {}

    @Override
	public void onLocationChanged(Location arg0) {}

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    public static class MyAlertDialogFragment extends DialogFragment {
		
	    @Override
	    public Dialog onCreateDialog(Bundle savedInstanceState) {
	    	final View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.datetimedialog, null);
			final EditText etDiveName = (EditText) dialogView.findViewById(R.id.etMapDiveName);
			final DatePicker datePickerMap = (DatePicker) dialogView.findViewById(R.id.datePickerMap);
			final TimePicker timePickerMap = (TimePicker) dialogView.findViewById(R.id.timePickerMap);

			return new AlertDialog.Builder(getActivity())
					.setTitle(R.string.title_map_marker)
					.setView(dialogView)
					.setNegativeButton(android.R.string.cancel, null)
					.setPositiveButton(android.R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int whichButton) {
									String divename = etDiveName.getText().toString();
									if (divename.isEmpty()) {
										divename = getString(R.string.default_map_dive_name);
									}
									int minute = timePickerMap.getCurrentMinute();
									int hour = timePickerMap.getCurrentHour();
									int year = datePickerMap.getYear();
									int month = datePickerMap.getMonth();
									int day = datePickerMap.getDayOfMonth();
									Calendar cal = new GregorianCalendar(year, month, day, hour, minute);
									cal.setTimeZone(TimeZone.getTimeZone("GMT"));
									((PickLocationMap) getActivity()).doPositiveClick(cal.getTime().getTime(), divename);
								}
					}).create();
	    }
	}
}
