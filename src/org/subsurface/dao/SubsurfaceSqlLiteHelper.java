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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Helper for DB creation / updates.
 * @author Aurelien PRALONG
 *
 */
class SubsurfaceSqlLiteHelper extends SQLiteOpenHelper {

	private static final String TAG = "SubsurfaceSqlLiteHelper";

	private static final String DATABASE_NAME = "subsurface.db";
	private static final int DATABASE_VERSION = 1;

	private static final String CREATE_TABLE_LOGS = "create table "
			+ DiveLocationLogDao.TABLE_NAME + "("
			+ DiveLocationLogDao.KEY_ID + " integer primary key autoincrement, "
			+ DiveLocationLogDao.KEY_LATITUDE + " numeric, "
			+ DiveLocationLogDao.KEY_LONGITUDE + " numeric, "
			+ DiveLocationLogDao.KEY_NAME + " text not null, "
			+ DiveLocationLogDao.KEY_TIMESTAMP + " integer);";

	SubsurfaceSqlLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(CREATE_TABLE_LOGS);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
		db.execSQL("DROP TABLE IF EXISTS " + DiveLocationLogDao.TABLE_NAME);
		onCreate(db);
	}
}
