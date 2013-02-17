package org.subsurface.ui;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.subsurface.R;
import org.subsurface.controller.DiveController;
import org.subsurface.model.DiveLocationLog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

public class DiveArrayAdapter extends ArrayAdapter<DiveLocationLog> {

	private class DiveFilter extends Filter {
		public long startDate;
		public long endDate;

		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			FilterResults results = new FilterResults();
			List<DiveLocationLog> list = DiveController.instance.getFilteredDives(
					constraint == null ? null : constraint.toString(), startDate, endDate, false);
			results.values = list;
			results.count = list.size();
			return results;
		}

		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {
			filteredLogs = (List<DiveLocationLog>) results.values;
			if (results.count > 0) {
				notifyDataSetChanged();
			} else {
				notifyDataSetInvalidated();
			}
		}
	}

	private static class ViewHolder {
		public CheckedTextView title;
		public TextView date;
		public TextView hour;
		public ImageView toUpload;
	}

	private final Context context;
	private final String dateFormat;
	private final String hourFormat;
	private final DiveFilter filter;
	private List<DiveLocationLog> filteredLogs;
	private boolean isFilterEnabled;

	public DiveArrayAdapter(Context context) {
		super(context, R.layout.dive_item);
		this.context = context;
		this.dateFormat = context.getString(R.string.dive_list_date);
		this.hourFormat = context.getString(R.string.dive_list_hour);
		this.filter = new DiveFilter();
		this.isFilterEnabled = false;
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
		holder.title.setText(dive.getName() + (dive.isSent() ? "" : " (Not sent)"));
		holder.date.setText(new SimpleDateFormat(dateFormat).format(new Date(dive.getTimestamp())));
		holder.hour.setText(new SimpleDateFormat(hourFormat).format(new Date(dive.getTimestamp())));
		//holder.toUpload.setVisibility(dive.isSent() ? View.INVISIBLE : View.VISIBLE);
		return rowView;
	}

	@Override
	public int getCount() {
		return isFilterEnabled && filteredLogs != null ? filteredLogs.size() : DiveController.instance.getDiveLogs().size();
	}

	@Override
	public DiveLocationLog getItem(int pos) {
		return isFilterEnabled && filteredLogs != null ? filteredLogs.get(pos) : DiveController.instance.getDiveLogs().get(pos);
	}

	public DiveFilter getFilter() {
		return filter;
	}

	public void filter(String name, long start, long end) {
		this.isFilterEnabled = true;
		filter.startDate = start;
		filter.endDate = end;
		filter.filter(name);
	}
}
