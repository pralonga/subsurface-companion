package org.subsurface.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Utility class for time manipulation.
 * @author Aurelien PRALONG
 *
 */
public class DateUtils {

	/**
	 * @return current date as UTC date (e.g. 12:30 at Tokyo will be 12:30 UTC)
	 */
	public static long getFakeUtcDate() {
		TimeZone currentTz = TimeZone.getDefault();
		Calendar fixed = Calendar.getInstance();
		fixed.setTimeZone(TimeZone.getTimeZone("GMT+0"));
		fixed.add(Calendar.MILLISECOND, currentTz.getRawOffset());
		if (currentTz.inDaylightTime(fixed.getTime())) {
			fixed.add(Calendar.MILLISECOND, currentTz.getDSTSavings());
		}
		fixed.set(Calendar.SECOND, 0);
		fixed.set(Calendar.MILLISECOND, 0);
		return fixed.getTimeInMillis();
	}

	/**
	 * Inits a new {@link SimpleDateFormat} at GMT.
	 * @param format format to use
	 * @return ready to use {@link SimpleDateFormat}
	 */
	public static SimpleDateFormat initGMT(String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		sdf.setTimeZone(TimeZone.getTimeZone("GMT+0"));
		return sdf;
	}
}
