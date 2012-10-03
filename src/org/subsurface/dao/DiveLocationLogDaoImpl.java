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

import java.util.ArrayList;
import java.util.List;

import org.subsurface.model.DiveLocationLog;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * DiveLocationDao implementation.
 * @author Aurelien PRALONG
 *
 */
class DiveLocationLogDaoImpl implements DiveLocationLogDao {

	private static final String TAG = "DiveLocationLogDaoImpl";
	private static final String[] ALL_COLUMNS = { KEY_ID, KEY_LATITUDE, KEY_LONGITUDE, KEY_NAME, KEY_TIMESTAMP };

	private SQLiteDatabase db;
	private SubsurfaceSqlLiteHelper helper;

	public DiveLocationLogDaoImpl(SubsurfaceSqlLiteHelper helper) {
		this.helper = helper;
	}

	private DiveLocationLog cursorToDiveLocationLog(Cursor cursor) {
		DiveLocationLog log = new DiveLocationLog();
		log.setId(cursor.getInt(0));
		log.setLatitude(cursor.getDouble(1));
		log.setLongitude(cursor.getDouble(2));
		log.setName(cursor.getString(3));
		log.setTimestamp(cursor.getLong(4));
		return log;
	}

	@Override
	public void addDiveLocationLog(DiveLocationLog diveLocationLog) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_LATITUDE, diveLocationLog.getLatitude());
		initialValues.put(KEY_LONGITUDE, diveLocationLog.getLongitude());
		initialValues.put(KEY_NAME, diveLocationLog.getName());
		initialValues.put(KEY_TIMESTAMP, diveLocationLog.getTimestamp());
		db.insert(TABLE_NAME, null, initialValues);
		Log.d(TAG, "Log added : " + diveLocationLog.toString());
	}

	@Override
	public void deleteDiveLocationLog(DiveLocationLog diveLocationLog) {
		db.delete(TABLE_NAME, KEY_ID + " = " + diveLocationLog.getId(), null);
		Log.d(TAG, "Log deleted : " + diveLocationLog.toString());
	}

	@Override
	public List<DiveLocationLog> getAllDiveLocationLogs() {
		List<DiveLocationLog> logs = new ArrayList<DiveLocationLog>();

		Cursor cursor = db.query(TABLE_NAME, ALL_COLUMNS, null, null, null, null, null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			DiveLocationLog comment = cursorToDiveLocationLog(cursor);
			logs.add(comment);
			cursor.moveToNext();
		}
		// Make sure to close the cursor
		cursor.close();
		Log.d(TAG, "Log read : " + logs.size());
		return logs;
	}

	public void open() {
		db = helper.getWritableDatabase();
	}

	public void close() {
		helper.close();
	}
}
