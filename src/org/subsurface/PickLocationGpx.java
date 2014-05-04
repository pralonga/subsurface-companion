package org.subsurface;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;

import org.subsurface.model.DiveLocationLog;
import org.subsurface.ui.GpxDiveListAdapter;
import org.subsurface.ui.GpxDiveListAdapter.CheckedState;
import org.subsurface.util.GpxParser;
import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
/**
 * Activity to choose the dive locations to import from GPX file
 * @author Venkatesh Shukla
 */
public class PickLocationGpx extends SherlockListActivity implements OnItemClickListener {

	private ListView listview;
	private ArrayList<DiveLocationLog> allgpxlogs;
	private Intent resultIntent;
	private Context context;

	private static final String GPX_DIVE_LOGS = "gpxdivelogs";
	private static final String GPX_FILE_PATH = "gpxfilepath";
	private static final String TAG = "PickLocationGpx";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		allgpxlogs = new ArrayList<DiveLocationLog>();
		resultIntent = new Intent();
		String gpx_filepath = getIntent().getStringExtra(GPX_FILE_PATH);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		getSupportActionBar().setTitle(R.string.title_picklocationgpx);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSherlock().setProgressBarIndeterminateVisibility(true);
		GetDivesFromGpx gdfg = new GetDivesFromGpx();
		gdfg.execute(gpx_filepath);
		listview = getListView();
		listview.setOnItemClickListener(this);
		context = this;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		CheckBox cbDive = (CheckBox) v.findViewById(R.id.cbGpxDive);
		cbDive.setChecked(!cbDive.isChecked());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.picklocation_gpx, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		GpxDiveListAdapter gpxDla = (GpxDiveListAdapter) getListAdapter();
		switch(item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		case R.id.gpxmenu_send:
			Bundle diveBundle = new Bundle();
			diveBundle.putParcelableArrayList(GPX_DIVE_LOGS, gpxDla.getSelectedDives());
			resultIntent.putExtras(diveBundle);
			setResult(Activity.RESULT_OK, resultIntent);
			finish();
			return true;
		case R.id.gpxmenu_select_all:
			CheckedState cs = gpxDla.getCheckedState();
			if(cs == CheckedState.ALLUNCHECKED || cs == CheckedState.MIXED) {
				gpxDla.setCheckedAll(true);
				item.setTitle(R.string.menu_unselect_all);
			} else {
				gpxDla.setCheckedAll(false);
				item.setTitle(R.string.menu_select_all);
			}
			gpxDla.notifyDataSetChanged();
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Async task which parses data of GPX file and sets the listAdapter
	 */
	private class GetDivesFromGpx extends AsyncTask<String, Void, Void>{
		private FileInputStream xmlstream;
		private GpxParser parser;

		@Override
		protected Void doInBackground(String ... paths ) {
			for(String path:paths) {
				try
				{
					xmlstream = new FileInputStream(path);
					parser = new GpxParser(context);
					allgpxlogs.addAll(parser.parse(xmlstream));
				}
				catch (FileNotFoundException e)
				{
					Log.e(TAG, e.toString());
				} catch (XmlPullParserException e) {
					Log.e(TAG, e.toString());
				} catch (IOException e) {
					Log.e(TAG, e.toString());
				} catch (ParseException e) {
					Log.e(TAG, e.toString());
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if(allgpxlogs.size() == 0) {
				//No dive logs found in gpx. Exit
				Toast.makeText(PickLocationGpx.this, R.string.error_gpx_no_dive_found, Toast.LENGTH_SHORT).show();
				setResult(Activity.RESULT_CANCELED, resultIntent);
				finish();
			}
			GpxDiveListAdapter adapter = new GpxDiveListAdapter(PickLocationGpx.this, allgpxlogs);
			setListAdapter(adapter);
			getSherlock().setProgressBarIndeterminateVisibility(false);
			super.onPostExecute(result);
		}
	}
}
