package org.subsurface.ui;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.subsurface.R;
import org.subsurface.controller.DiveController;
import org.subsurface.model.DiveLocationLog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.TextView;

public class DiveArrayAdapter extends ArrayAdapter<DiveLocationLog> {

	private final Context context;
	private final String dateFormat;
	private final String hourFormat;

	private static class ViewHolder {
		public CheckedTextView title;
		public TextView date;
		public TextView hour;
		public ImageView toUpload;
	}

	public DiveArrayAdapter(Context context) {
		super(context, R.layout.dive_item);
		this.context = context;
		this.dateFormat = context.getString(R.string.dive_list_date);
		this.hourFormat = context.getString(R.string.dive_list_hour);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = convertView;
		if (rowView == null) {
			LayoutInflater inflater = LayoutInflater.from(context);
			rowView = inflater.inflate(R.layout.dive_item, null);
			ViewHolder viewHolder = new ViewHolder();
			viewHolder.title = (CheckedTextView) rowView.findViewById(android.R.id.title);
			viewHolder.date = (TextView) rowView.findViewById(android.R.id.text1);
			viewHolder.hour = (TextView) rowView.findViewById(android.R.id.text2);
			viewHolder.toUpload = (ImageView) rowView.findViewById(android.R.id.icon);
			rowView.setTag(viewHolder);
		}

		ViewHolder holder = (ViewHolder) rowView.getTag();
		DiveLocationLog dive = (DiveLocationLog) getItem(position);
		holder.title.setText(dive.getName());
		holder.date.setText(new SimpleDateFormat(dateFormat).format(new Date(dive.getTimestamp())));
		holder.hour.setText(new SimpleDateFormat(hourFormat).format(new Date(dive.getTimestamp())));
		holder.toUpload.setVisibility(dive.isSent() ? View.INVISIBLE : View.VISIBLE);
		return rowView;
	}

	@Override
	public int getCount() {
		return DiveController.instance.getDiveLogs().size();
	}

	@Override
	public DiveLocationLog getItem(int pos) {
		return DiveController.instance.getDiveLogs().get(pos);
	}
}
