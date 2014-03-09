package org.subsurface.ui;

import java.util.ArrayList;

import org.subsurface.R;
import org.subsurface.model.GpxFileInfo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class GpxListAdapter extends BaseAdapter
{
	private final ArrayList<GpxFileInfo> gpxfiles;
	private final LayoutInflater inflater;

	public GpxListAdapter(Context context, ArrayList<GpxFileInfo> gpxfiles)	{
		this.gpxfiles = gpxfiles;
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public void addItem(final GpxFileInfo gpxfileinfo) {
		gpxfiles.add(gpxfileinfo);
        super.notifyDataSetChanged();
    }

	@Override
	public int getCount() {
		return gpxfiles.size();
	}
	@Override
	public Object getItem(int position) {
		return gpxfiles.get(position);
	}
	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		 ViewHolder holder = null;
         if (convertView == null) {
             convertView = inflater.inflate(R.layout.gpx_listitem, parent, false);
             holder = new ViewHolder();
             holder.tvName = (TextView)convertView.findViewById(R.id.tvGpxName);
             holder.tvDirectory = (TextView)convertView.findViewById(R.id.tvGpxDirectory);
             holder.tvDate = (TextView)convertView.findViewById(R.id.tvGpxDate);
             convertView.setTag(holder);
         } else {
             holder = (ViewHolder)convertView.getTag();
         }
         holder.tvName.setText(gpxfiles.get(position).getName());
         holder.tvDirectory.setText(gpxfiles.get(position).getDirectory());
         holder.tvDate.setText(gpxfiles.get(position).getDate());
         return convertView;
	}

	public static class ViewHolder {
		public TextView tvName;
		public TextView tvDirectory;
		public TextView tvDate;
	}
}

