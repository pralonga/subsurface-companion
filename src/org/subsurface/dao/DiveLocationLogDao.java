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
package org.subsurface.dao;

import java.util.List;

import org.subsurface.model.DiveLocationLog;

/**
 * DAO for DiveLocationLog.
 * @author Aurelien PRALONG
 *
 */
public interface DiveLocationLogDao {

	String TABLE_NAME = "locations";
	String KEY_ID = "_id";
	String KEY_LATITUDE = "latitude";
	String KEY_LONGITUDE = "longitude";
	String KEY_TIMESTAMP = "timestamp";
	String KEY_NAME = "name";
	String KEY_SENT = "sent";

	/**
	 * Adds a log in DB.
	 * @param diveLocationLog log to add
	 */
	void save(DiveLocationLog diveLocationLog);

	/**
	 * Deletes log from DB.
	 * @param diveLocationLog log to delete
	 */
	void deleteDiveLocationLog(DiveLocationLog diveLocationLog);

	/**
	 * @return all logs in DB
	 */
	List<DiveLocationLog> getAllDiveLocationLogs();

	/**
	 * Finds a dive from it date.
	 * @param date date to search
	 * @return found dive, or null if none
	 */
	DiveLocationLog find(long date);

	/**
	 * Deletes all records.
	 */
	void deleteAll();

	/**
	 * Opens DB.
	 */
	void open();

	/**
	 * Closes DB.
	 */
	void close();
}
