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

import android.location.Location;

/**
 * Model for GPS location.
 * @author Aurelien PRALONG
 *
 */
public class DiveLocationLog {

	private int id;
	private double latitude;
	private double longitude;
	private long timestamp;
	private String name;

	/**
	 * Default constructor.
	 */
	public DiveLocationLog() {
		this.latitude = 0;
		this.longitude = 0;
		this.timestamp = 0;
		this.name = null;
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

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	@Override
	public String toString() {
		return new StringBuilder().append(id).append('/')
				.append(name).append('/')
				.append(timestamp).append('/')
				.append(latitude).append('/')
				.append(longitude).toString();
	}
}
