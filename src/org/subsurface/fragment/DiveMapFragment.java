package org.subsurface.fragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.subsurface.DiveDetailActivity;
import org.subsurface.R;
import org.subsurface.controller.DiveController;
import org.subsurface.model.DiveLocationLog;
import org.subsurface.util.DateUtils;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class DiveMapFragment extends SupportMapFragment implements DiveReceiver {

	private static final String TAG = "DiveMapFragment";
	private Map<Marker, Integer> allMarkersMap = new HashMap<Marker, Integer>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View result = super.onCreateView(inflater, container, savedInstanceState);
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());
        if (resultCode != ConnectionResult.SUCCESS) {
        	GooglePlayServicesUtil.getErrorDialog(resultCode, getActivity(), 0).show();
        } else {
        	onRefreshDives();
        }
        return result;
	}

	@Override
	public void onRefreshDives() {
		GoogleMap map = getMap();
		if (map != null) {
			SimpleDateFormat formatter = DateUtils.initGMT(getString(R.string.date_format_full));
			allMarkersMap.clear();
			map.clear();
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
					Intent detailIntent = new Intent(getActivity(), DiveDetailActivity.class);
					detailIntent.putExtra(DiveDetailActivity.PARAM_DIVE_POSITION, allMarkersMap.get(marker));
					startActivity(detailIntent);
				}
			});
		} else {
			Log.w(TAG, "Could not get GoogleMap object");
		}
	}
}
