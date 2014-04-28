package org.subsurface.util;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import org.subsurface.model.DiveLocationLog;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.location.Location;
import android.util.Xml;
/**
 * Parser for Gpx files
 * @author Venkatesh Shukla
 */

public class GpxParser {

	private static final String ns = null;
	private static final String DATEFORMAT = "yyyy-MM-dd'T'HH:mm:ss";
	/**
	 * Parse the GPX file
	 * @param in InputStream of the GPX file
	 * @return ArrayList of DiveLocationLogs present in the GPX file
	 * @throws XmlPullParserException
	 * @throws IOException
	 * @throws ParseException
	 */
	public ArrayList<DiveLocationLog> parse(InputStream in) throws XmlPullParserException, IOException, ParseException {
		try {
			XmlPullParser parser = Xml.newPullParser();
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			parser.setInput(in, null);
			parser.nextTag();
			return readFeed(parser);
		} finally {
			in.close();
		}
	}
	/**
	 * Read and parse the GPX to get DiveLocationLogs
	 * @param parser The XmlPullParser in its present state
	 * @return ArrayList of all the divepoints(as Waypoints) found in the GPX file.
	 * @throws XmlPullParserException
	 * @throws IOException
	 * @throws ParseException
	 */
	private ArrayList<DiveLocationLog> readFeed(XmlPullParser parser) throws XmlPullParserException, IOException, ParseException {
		ArrayList<DiveLocationLog> alldivelogs = new ArrayList<DiveLocationLog>();
		parser.require(XmlPullParser.START_TAG, ns, "gpx");
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = parser.getName();
			// Only takes care of waypoints in the gpx
			if (name.equals("wpt")) {
				Location diveloc = readLocation(parser);
				DiveLocationLog dll = readEntry(parser);
				dll.setLocation(diveloc);
				alldivelogs.add(dll);
			} else {
				skip(parser);
			}
		}
		return alldivelogs;
	}

	/**
	 * Parses the contents of an entry. If it encounters a name or time, hands them off
	 * to their respective "read" methods for processing. Otherwise, skips the tag.
	 * @param parser The XmlPullParser in its present state
	 * @return DiveLocationLog containing Name and Time entries
	 * @throws XmlPullParserException
	 * @throws IOException
	 * @throws ParseException
	 */
	// 
	private DiveLocationLog readEntry(XmlPullParser parser)	throws XmlPullParserException, IOException, ParseException {
		parser.require(XmlPullParser.START_TAG, ns, "wpt");
		String divename = null, divesym = null;
		Location diveloc = new Location("");
		long divetimestamp = -1;
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = parser.getName();
			if (name.equals("name")) {
				divename = readName(parser);
			} else if(name.equals("time")) {
				divetimestamp = readTime(parser);
			} else if(name.equals("sym")) {
				divesym = readSym(parser);
			} else {
				skip(parser);
			}
		}
		return new DiveLocationLog(diveloc, String.format("%s (%s)", divename, divesym), divetimestamp);
	}

	/**
	 * Processes title tags in the feed.
	 * @param parser The XmlPullParser in its present state
	 * @return text present in the name tag of the gpx
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	private String readName(XmlPullParser parser) throws IOException, XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, ns, "name");
		String title = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, "name");
		return title;
	}
	
	/**
	 * Processes sym tags in the feed.
	 * @param parser The XmlPullParser in its present state
	 * @return text present in the sym tag of the gpx
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	private String readSym(XmlPullParser parser) throws IOException, XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, ns, "sym");
		String sym = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, "sym");
		return sym;
	}
	
	/**
	 * Extracts latitude and longitude attributr from wpt tag in the feed.
	 * @param parser The XmlPullParser in its present state
	 * @return Location of the waypoint extracted from the attributes of wpt tag
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	private Location readLocation(XmlPullParser parser) throws IOException, XmlPullParserException {
		Location diveloc = new Location("");
		Double divelat, divelon;
		parser.require(XmlPullParser.START_TAG, ns, "wpt");
		divelat = Double.valueOf(parser.getAttributeValue(null, "lat"));
		divelon = Double.valueOf(parser.getAttributeValue(null, "lon"));
		diveloc.setLatitude(divelat);
		diveloc.setLongitude(divelon);
		return diveloc;
	}

	/**
	 * Extracts time from the GPX given in ISO-8601 format and converts to UTC timestamp
	 * @param parser The XmlPullParser in its present state
	 * @return timestamp of the dive extracted from the time tag of gpx
	 * @throws IOException
	 * @throws XmlPullParserException
	 * @throws ParseException
	 */
	private long readTime(XmlPullParser parser) throws IOException, XmlPullParserException, ParseException {
		parser.require(XmlPullParser.START_TAG, ns, "time");
		String time = readText(parser);
		long timestamp = 0;
		SimpleDateFormat isotime = new SimpleDateFormat(DATEFORMAT, Locale.getDefault());
		timestamp = isotime.parse(time).getTime();
		parser.require(XmlPullParser.END_TAG, ns, "time");
		return timestamp;
	}

	/**
	 * For the tags name and time extracts their text values.
	 * @param parser The XmlPullParser in its present state
	 * @return the text present in the present tag
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
		String result = "";
		if (parser.next() == XmlPullParser.TEXT) {
			result = parser.getText();
			parser.nextTag();
		}
		return result;
	}

	/**
	 * Skips the tag names not require
	 * @param parser The XmlPullParser in its present state
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
		if (parser.getEventType() != XmlPullParser.START_TAG) {
			throw new IllegalStateException();
		}
		int depth = 1;
		while (depth != 0) {
			switch (parser.next()) {
			case XmlPullParser.END_TAG:
				depth--;
				break;
			case XmlPullParser.START_TAG:
				depth++;
				break;
			}
		}
	}
}
