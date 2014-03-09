package org.subsurface;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.subsurface.model.DiveLocationLog;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.location.Location;
import android.util.Xml;

public class GpxParser {

	private static final String ns = null;
	SimpleDateFormat isotime;

	public GpxParser() {
		super();
		isotime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss",
				Locale.getDefault());
	}

	public List<DiveLocationLog> parse(InputStream in) throws XmlPullParserException, IOException, ParseException {
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

	private List<DiveLocationLog> readFeed(XmlPullParser parser) throws XmlPullParserException, IOException, ParseException {
		List<DiveLocationLog> alldivelogs = new ArrayList<DiveLocationLog>();
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

	// Parses the contents of an entry. If it encounters a name or time, hands them off
	// to their respective "read" methods for processing. Otherwise, skips the tag.
	private DiveLocationLog readEntry(XmlPullParser parser)	throws XmlPullParserException, IOException, ParseException {
		parser.require(XmlPullParser.START_TAG, ns, "wpt");
		String divename = null;
		Location diveloc = new Location("");
		long divetimestamp = -1;
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = parser.getName();
			if (name.equals("name")) {
				divename = readName(parser);
			} else if (name.equals("time")) {
				divetimestamp = readTime(parser);
			} else {
				skip(parser);
			}
		}
		return new DiveLocationLog(diveloc, divename, divetimestamp);
	}

	// Processes title tags in the feed.
	private String readName(XmlPullParser parser) throws IOException, XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, ns, "name");
		String title = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, "name");
		return title;
	}

	// Extracts latitude and longitude attributr from wpt tag in the feed.
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

	// Extracts time from the GPX given in ISO-8601 format and converts to UTC timestamp
	private long readTime(XmlPullParser parser) throws IOException, XmlPullParserException, ParseException {
		parser.require(XmlPullParser.START_TAG, ns, "time");
		String time = readText(parser);
		long timestamp = 0;
		timestamp = isotime.parse(time).getTime();
		parser.require(XmlPullParser.END_TAG, ns, "time");
		return timestamp;
	}

	// For the tags name and time extracts their text values.
	private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
		String result = "";
		if (parser.next() == XmlPullParser.TEXT) {
			result = parser.getText();
			parser.nextTag();
		}
		return result;
	}

	// Skips the tag names not required
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
