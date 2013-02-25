package org.subsurface.ui;

import android.app.TimePickerDialog;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;

/**
 * Button / TimePicker link abstraction.
 * @author Aurelien PRALONG
 *
 */
public class TimePickerButton implements TimePickerDialog.OnTimeSetListener, View.OnClickListener {

	public static interface TimeSetListener {
		public void onTimeSet(Button button, int minutes);
	}

	private int totalMinutes;
	private Button associatedButton;
	private TimeSetListener listener;

	public TimePickerButton(Button button, int initialMinutes, TimeSetListener listener) {
		this.associatedButton = button;
		this.totalMinutes = initialMinutes;
		this.listener = listener;
	}

	/**
	 * Formats a duration.
	 * @param duration duration in minutes
	 * @return formated time (Format : XX:YY)
	 */
	public static String getDurationText(long duration) {
		long hours = duration / 60;
		long minutes = duration % 60;
		return (hours < 10 ? "0" : "") + hours
				+ ":"
				+ (minutes < 10 ? "0" : "") + minutes;
	}

	public static TimePickerButton initButton(Button button, int initialMinutes, TimeSetListener listener) {
		TimePickerButton rootListener = new TimePickerButton(button, initialMinutes, listener);
		button.setOnClickListener(rootListener);
		button.setText(getDurationText(initialMinutes));
		return rootListener;
	}

	@Override
	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		totalMinutes = hourOfDay * 60 + minute;
		associatedButton.setText(getDurationText(totalMinutes));
		if (listener != null) {
			listener.onTimeSet(associatedButton, totalMinutes);
		}
	}

	@Override
	public void onClick(View v) {
		new TimePickerDialog(associatedButton.getContext(), this, totalMinutes / 60, totalMinutes % 60, true).show();
	}
}
