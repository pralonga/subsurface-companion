package org.subsurface.ui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import org.subsurface.R;
import org.subsurface.model.DiveLocationLog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

public class GpxDiveListAdapter extends BaseAdapter {

	private final ArrayList<DiveLocationLog> gpxdivelogs;
	private final LayoutInflater inflater;
	private final SimpleDateFormat sdfdate;
	private final SimpleDateFormat sdftime;
	private final boolean[] cbState; // boolean array to save which checkboxes are checked

	public GpxDiveListAdapter(Context context, ArrayList<DiveLocationLog> gpxdivelogs)	{
		this.gpxdivelogs = gpxdivelogs;
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		sdfdate = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
		sdftime = new SimpleDateFormat("HH:mm", Locale.getDefault());
		cbState = new boolean[gpxdivelogs.size()];

	}

	public void addItem(final DiveLocationLog gpxdivelog) {
		gpxdivelogs.add(gpxdivelog);
		super.notifyDataSetChanged();
    }

	@Override
	public int getCount() {
		return gpxdivelogs.size();
	}

	@Override
	public Object getItem(int position) {
		return gpxdivelogs.get(position);
	}

	public ArrayList<DiveLocationLog> getSelectedDives() {
		ArrayList<DiveLocationLog> selectedDives = new ArrayList<DiveLocationLog>();
		for(int i = 0; i < gpxdivelogs.size(); i++) {
			if(cbState[i]) {
				selectedDives.add(gpxdivelogs.get(i));
			}
		}
		return selectedDives;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		 ViewHolder holder = null;
		 long timestamp;
         if (convertView == null) {
             convertView = inflater.inflate(R.layout.gpx_divelistitem, parent, false);
             holder = new ViewHolder();
             holder.tvGpxDiveName = (TextView) convertView.findViewById(R.id.tvGpxDiveName);
             holder.tvGpxDiveDate = (TextView) convertView.findViewById(R.id.tvGpxDiveDate);
             holder.tvGpxDiveTime = (TextView) convertView.findViewById(R.id.tvGpxDiveTime);
             holder.tvGpxDiveLocation = (TextView) convertView.findViewById(R.id.tvGpxDiveLoc);
             holder.cbGpxDive = (CheckBox) convertView.findViewById(R.id.cbGpxDive);
         } else {
             holder = (ViewHolder) convertView.getTag();
             holder.cbGpxDive.setOnCheckedChangeListener(null);
         }
         holder.tvGpxDiveName.setText(gpxdivelogs.get(position).getName());
         timestamp = gpxdivelogs.get(position).getTimestamp();
         holder.tvGpxDiveDate.setText(sdfdate.format(timestamp));
         holder.tvGpxDiveTime.setText(sdftime.format(timestamp));
         holder.cbGpxDive.setChecked(cbState[position]);
         holder.tvGpxDiveLocation.setText(gpxdivelogs.get(position).getLatitude() + " , " + gpxdivelogs.get(position).getLongitude());
         holder.cbGpxDive.setOnCheckedChangeListener(new OnCheckedChangeListener()
         {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				cbState[position] = isChecked;
			}
         });
         convertView.setTag(holder);
         return convertView;
	}

	public static class ViewHolder {
		public TextView tvGpxDiveName;
		public TextView tvGpxDiveDate;
		public TextView tvGpxDiveTime;
		public TextView tvGpxDiveLocation;
		public CheckBox cbGpxDive;
	}

}
