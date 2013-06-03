package org.subsurface.util;

import org.subsurface.R;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

/**
 * Utility class to handle GPS related things.
 * 
 * @author Sergey Starosek
 *
 */
public class GpsUtil {

	/**
	 * Returns localized string for the given coordinates.
	 * 
	 * @param ctx context
	 * @param lat latitude
	 * @param lon longitude
	 * @return String presentation of the given position
	 */
	public static String buildCoordinatesString(Context ctx, double lat, double lon) {
		String slat = ctx.getString(lat < 0 ? R.string.cardinal_south : R.string.cardinal_north);
		String slon = ctx.getString(lon < 0 ? R.string.cardinal_west : R.string.cardinal_east);
		
		lat = Math.abs(lat);
		lon = Math.abs(lon);
		
		return ctx.getString(R.string.details_coordinates, lat, lon, slat, slon);
	}
	
	/**
	 * Create new ACTION_VIEW geo URI intent for the given coordinates.
	 * 
	 * @param lat latitude
	 * @param lon longitude
	 * @return new intent
	 */
	public static Intent getGeoIntent(double lat, double lon) {
		String uri = "geo:" + lat + "," + lon;
		return new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
	}
}
