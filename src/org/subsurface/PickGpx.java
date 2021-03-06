package org.subsurface;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

import org.subsurface.model.DiveLocationLog;
import org.subsurface.model.GpxFileInfo;
import org.subsurface.ui.GpxListAdapter;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.NavUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
/**
 * Activity for choosing Gpx file present in the SD Card
 * @author Venkatesh Shukla
 */
public class PickGpx extends SherlockListActivity implements OnItemClickListener
{
	private ArrayList<GpxFileInfo> allgpxfiles;
	private File sd_card;
	private GpxListAdapter gla;
	private static final int PICK_GPX_LOCATION_REQCODE = 997;
	private static final String GPX_DIVE_LOGS = "gpxdivelogs";
	private static final String GPX_FILE_PATH = "gpxfilepath";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		getSupportActionBar();
		getSherlock().setProgressBarIndeterminateVisibility(true);
		getSupportActionBar().setTitle(R.string.title_pickgpx);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		allgpxfiles = new ArrayList<GpxFileInfo>();
		createAdapter();
		ListView listview = getListView();
		listview.setOnItemClickListener(this);
	}

	/**
	 * Find all gpx files present in the sd card and make a ListAdapter from them
	 */
	private void createAdapter() {
		String sd_state = Environment.getExternalStorageState();
		if(sd_state.contentEquals(Environment.MEDIA_MOUNTED) || sd_state.contentEquals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
			sd_card = Environment.getExternalStorageDirectory();
			GetAllGpx getallgpx = new GetAllGpx();
			getallgpx.execute(sd_card);
		} else {
			//SD card is unavailable, or unreadable
			Toast.makeText(this, R.string.error_sd_unavailable, Toast.LENGTH_SHORT).show();
			Intent resultIntent = new Intent();
			setResult(Activity.RESULT_CANCELED, resultIntent);
			finish();
		}
	}

	/**
	 * Recursively get all the GPX files in the SD card
	 * @param file Gpx file to be parsed
	 * @return ArrayList containing all GpxFileInfo
	 */
	private ArrayList<GpxFileInfo> getGpxInDir(File file) {
		ArrayList<GpxFileInfo> gpxfiles = new ArrayList<GpxFileInfo>();
		File[] filelist =  file.listFiles();
		for(File f: filelist) {
			if(!f.getName().startsWith(".")) {
				// Leave out hidden directories
				if(f.isDirectory())	{
					gpxfiles.addAll(getGpxInDir(f));
				}
				else if(f.getName().toLowerCase(Locale.getDefault()).endsWith(".gpx")) {
					String name = f.getName();
					String path = f.getPath();
					String directory = f.getParent()
							.replace(sd_card.getPath(), "~/")
							.replace("//", "/");
					long timestamp = f.lastModified();
					gpxfiles.add(new GpxFileInfo(name, path, timestamp, directory));
				}
			}
		}
		return gpxfiles;
	}

	/**
	 * Asynchronous task of getting all Gpx files in the SD card and updating the ListAdapter 
	 */
	private class GetAllGpx extends AsyncTask<File, Void, Void> {

		@Override
		protected Void doInBackground(File... allfiles) {
			//TODO find a way to add items as they are found in the list using notifyDataSetChange
			for(File file: allfiles) {
				allgpxfiles = getGpxInDir(file);
			}
			return null;
	    }

		@Override
		protected void onPostExecute(Void result) {
			if(allgpxfiles.size() == 0)	{
				// No GPX files found in the SD card
				Toast.makeText(PickGpx.this, getString(R.string.error_gpx_no_file_found), Toast.LENGTH_SHORT).show();
				Intent resultIntent = new Intent();
				setResult(Activity.RESULT_CANCELED, resultIntent);
				finish();
			} else {
				// Found some gpx files
				getSherlock().setProgressBarIndeterminateVisibility(false);
				gla = new GpxListAdapter(PickGpx.this, allgpxfiles);
				setListAdapter(gla);
			}
			super.onPostExecute(result);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Intent parseGpxIntent = new Intent(this,PickLocationGpx.class);
		parseGpxIntent.putExtra(GPX_FILE_PATH, allgpxfiles.get(position).getPath());
		startActivityForResult(parseGpxIntent, PICK_GPX_LOCATION_REQCODE);
	}
	
	@Override
	public void onActivityResult(int requestcode, int resultCode, Intent data) {
		if(resultCode == RESULT_OK && data != null) {
			Bundle rec_bundle = data.getExtras();
			switch(requestcode) {
			case PICK_GPX_LOCATION_REQCODE:
				ArrayList<DiveLocationLog> gpxdivelogs = (ArrayList<DiveLocationLog>) rec_bundle.get(GPX_DIVE_LOGS);
				Intent resultIntent = new Intent();
				Bundle diveBundle = new Bundle();
				diveBundle.putParcelableArrayList(GPX_DIVE_LOGS, gpxdivelogs);
				resultIntent.putExtras(diveBundle);
				setResult(Activity.RESULT_OK, resultIntent);
				finish();
				return;
			 }
		} else { // either some error has occurred or no data has been received
			Toast.makeText(this, R.string.error_no_dive_found, Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
