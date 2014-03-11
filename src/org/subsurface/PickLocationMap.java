package org.subsurface;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;

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
	private static final String LOCATION = "location";
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
			Intent resultIntent = new Intent();
			resultIntent.putExtra(LOCATION, latlng);
			setResult(Activity.RESULT_OK, resultIntent);
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
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

}
