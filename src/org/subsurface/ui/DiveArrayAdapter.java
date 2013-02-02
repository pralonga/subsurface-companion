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
import android.widget.BaseAdapter;
import android.widget.TextView;

public class DiveArrayAdapter extends BaseAdapter {

	private final Context context;
	private final String dateFormat;

	private static class ViewHolder {
		public TextView text1;
		public TextView text2;
	}

	public DiveArrayAdapter(Context context) {
		super();
		this.context = context;
		this.dateFormat = context.getString(R.string.date_format);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = convertView;
		if (rowView == null) {
			LayoutInflater inflater = LayoutInflater.from(context);
			rowView = inflater.inflate(R.layout.dive_item, null);
			ViewHolder viewHolder = new ViewHolder();
			viewHolder.text1 = (TextView) rowView.findViewById(android.R.id.text1);
			viewHolder.text2 = (TextView) rowView.findViewById(android.R.id.text2);
			rowView.setTag(viewHolder);
		}

		ViewHolder holder = (ViewHolder) rowView.getTag();
		DiveLocationLog dive = (DiveLocationLog) getItem(position);
		holder.text1.setText(dive.getName());
		holder.text2.setText(new SimpleDateFormat(dateFormat).format(new Date(dive.getTimestamp())));
		return rowView;
	}

	@Override
	public int getCount() {
		return DiveController.instance.getDiveLogs().size();
	}

	@Override
	public Object getItem(int pos) {
		return DiveController.instance.getDiveLogs().get(pos);
	}

	@Override
	public long getItemId(int pos) {
		return getItem(pos).hashCode();
	}
}
