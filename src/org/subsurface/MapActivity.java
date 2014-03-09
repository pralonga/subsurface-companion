package org.subsurface;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.subsurface.controller.DiveController;
import org.subsurface.model.DiveLocationLog;
import org.subsurface.util.DateUtils;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends SherlockFragmentActivity implements OnNavigationListener {

	private Map<Marker, Integer> allMarkersMap = new HashMap<Marker, Integer>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		ArrayAdapter<CharSequence> listAdapter = ArrayAdapter.createFromResource(
				this, R.array.list_menu_choices,
				R.layout.spinner_item);
		listAdapter.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);
		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		getSupportActionBar().setListNavigationCallbacks(listAdapter, this);
		getSupportActionBar().setSelectedNavigationItem(1);
		getSupportActionBar().setTitle(null);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		setContentView(R.layout.dive_map);
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
        	GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0).show();
        } else {
        	GoogleMap map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
    		if (map != null) {
    			SimpleDateFormat formatter = DateUtils.initGMT(getString(R.string.date_format_full));
    			List<DiveLocationLog> dives = DiveController.instance.getDiveLogs();
    			for (int i = 0; i < dives.size(); ++i) {
    				DiveLocationLog dive = dives.get(i);
    				Marker marker = map.addMarker(new MarkerOptions()
    						.position(new LatLng(dive.getLatitude(), dive.getLongitude()))
    						.title(dive.getName())
    						.snippet(formatter.format(new Date(dive.getTimestamp()))));
    				allMarkersMap.put(marker, i);
    			}
    			map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
					@Override
					public void onInfoWindowClick(Marker marker) {
						Intent detailIntent = new Intent(MapActivity.this, DiveDetailActivity.class);
						detailIntent.putExtra(DiveDetailActivity.PARAM_DIVE_POSITION, allMarkersMap.get(marker));
						startActivity(detailIntent);
					}
				});
    		}
        }
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		boolean handled = false;
		if (itemPosition == 0) {
			finish();
			handled = true;
		} else if (itemPosition == 1) {
			handled = true;
		}
		return handled;
	}

	@Override
	public void finish() {
		super.finish();
		// Disable out animation
		overridePendingTransition(0, 0);
	}
}
