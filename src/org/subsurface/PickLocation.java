package org.subsurface;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class PickLocation extends SherlockFragmentActivity implements OnMapClickListener
{

    private GoogleMap mMap;
    public static LatLng latlng;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dive_picklocation);
        setUpMapIfNeeded();
    }

    @Override
	public boolean onCreateOptionsMenu(Menu menu) 
    {
    	getSupportMenuInflater().inflate(R.menu.dive_loc_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
    
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		switch(item.getItemId())
		{
		case R.id.dive_loc_finish:
			Intent resultIntent = new Intent();
			resultIntent.putExtra("location", latlng);
			setResult(Activity.RESULT_OK, resultIntent);
			finish();
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() 
    {
        if (mMap == null) 
        {
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            if (mMap != null) 
            {
                setUpMap();
            }
        }
    }

    private void setUpMap() 
    {
        mMap.setOnMapClickListener(this);
    }

   	@Override
	public void onMapClick(LatLng loc) 
	{
		mMap.clear();
		mMap.addMarker(new MarkerOptions().position(loc).title("Dive Spot"));
		latlng = loc;
	}
	
}
