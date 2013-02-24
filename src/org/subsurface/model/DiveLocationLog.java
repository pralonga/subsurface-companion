/* Subsurface for Android
 * Copyright (C) 2012  Aurelien PRALONG
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.subsurface.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import android.location.Location;

/**
 * Model for GPS location.
 * @author Aurelien PRALONG
 *
 */
@DatabaseTable(tableName = "dives")
public class DiveLocationLog {

	public static final String KEY_ID = "_id";
	public static final String KEY_LATITUDE = "latitude";
	public static final String KEY_LONGITUDE = "longitude";
	public static final String KEY_TIMESTAMP = "timestamp";
	public static final String KEY_NAME = "name";
	public static final String KEY_SENT = "sent";
	public static final String KEY_HIDDEN = "hidden";

	@DatabaseField(generatedId = true, columnName = KEY_ID)
	private Long id;

	@DatabaseField(columnName = KEY_LATITUDE)
	private double latitude;

	@DatabaseField(columnName = KEY_LONGITUDE)
	private double longitude;

	@DatabaseField(columnName = KEY_TIMESTAMP)
	private long timestamp;

	@DatabaseField(columnName = KEY_NAME)
	private String name;

	@DatabaseField(columnName = KEY_SENT)
	private boolean sent;

	@DatabaseField(columnName = KEY_HIDDEN)
	private boolean hidden;

	/**
	 * Default constructor.
	 */
	public DiveLocationLog() {
		this.id = 0L;
		this.latitude = 0;
		this.longitude = 0;
		this.timestamp = 0;
		this.name = null;
		this.sent = false;
	}

	/**
	 * Builds a log from given location / timestamp.
	 * @param location location retrieved
	 * @param name location name
	 * @param timestamp log UTC date
	 */
	public DiveLocationLog(Location location, String name, long timestamp) {
		this.latitude = location.getLatitude();
		this.longitude = location.getLongitude();
		this.name = name;
		this.timestamp = timestamp;
		this.sent = false;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLocation(Location location) {
		this.latitude = location.getLatitude();
		this.longitude = location.getLongitude();
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}

	public void setSent(boolean sent) {
		this.sent = sent;
	}

	public boolean isSent() {
		return sent;
	}

	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	@Override
	public String toString() {
		return new StringBuilder().append(id).append('/')
				.append(name).append('/')
				.append(timestamp).append('/')
				.append(latitude).append('/')
				.append(longitude).append('/')
				.append(sent).toString();
	}
}
