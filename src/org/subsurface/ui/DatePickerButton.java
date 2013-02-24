package org.subsurface.ui;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.subsurface.R;

import android.app.DatePickerDialog;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;

public class DatePickerButton implements DatePickerDialog.OnDateSetListener, View.OnClickListener {

	public static interface DateSetListener {
		public void onDateSet(Button button, long date);
	}

	private long currentDate;
	private Button associatedButton;
	private DateSetListener listener;

	public DatePickerButton(Button button, long initialDate, DateSetListener listener) {
		this.associatedButton = button;
		Calendar initCal = Calendar.getInstance();
		initCal.setTimeInMillis(initialDate);
		initCal.set(Calendar.HOUR_OF_DAY, 0);
		initCal.set(Calendar.MINUTE, 0);
		initCal.set(Calendar.SECOND, 0);
		initCal.set(Calendar.MILLISECOND, 0);
		this.currentDate = initCal.getTimeInMillis();
		this.listener = listener;
	}

	public static String getDateText(long currentDate, String dateFormat) {
		return new SimpleDateFormat(dateFormat).format(new Date(currentDate));
	}

	public static DatePickerButton initButton(Button button, long initialDate, DateSetListener listener) {
		DatePickerButton rootListener = new DatePickerButton(button, initialDate, listener);
		button.setOnClickListener(rootListener);
		button.setText(getDateText(initialDate, button.getContext().getString(R.string.date_format_short)));
		return rootListener;
	}

	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
		currentDate = new GregorianCalendar(year, monthOfYear, dayOfMonth).getTimeInMillis();
		associatedButton.setText(getDateText(currentDate, associatedButton.getContext().getString(R.string.date_format_short)));
		if (listener != null) {
			listener.onDateSet(associatedButton, currentDate);
		}
	}

	@Override
	public void onClick(View v) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(currentDate);
		new DatePickerDialog(associatedButton.getContext(), this, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE)).show();
	}
}
