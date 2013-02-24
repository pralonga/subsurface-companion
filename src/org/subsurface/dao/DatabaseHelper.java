package org.subsurface.dao;

import java.sql.SQLException;

import org.subsurface.model.DiveLocationLog;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

	private static final String DATABASE_NAME = "subsurface.db";
	public static final int DATABASE_VERSION = 4;

	private Dao<DiveLocationLog, Long> diveDao;

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		// TODO Set hardcoded schema, for enhanced performances
		//super(context, DATABASE_NAME, null, DATABASE_VERSION, R.raw.ormlite_config);
	}

	@Override
	public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
		try {
			TableUtils.createTable(connectionSource, DiveLocationLog.class);
		} catch (SQLException e) {
			Log.e(getClass().getName(), "Unable to create databases", e);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
		try {
			TableUtils.dropTable(connectionSource, DiveLocationLog.class, true);
			onCreate(database, connectionSource);
		} catch (SQLException e) {
			Log.e(getClass().getName(),
					"Unable to upgrade from version " + oldVersion + " to new " + newVersion, e);
		}
	}

	public void resetDives() {
		try {
			TableUtils.dropTable(connectionSource, DiveLocationLog.class, true);
			TableUtils.createTable(connectionSource, DiveLocationLog.class);
		} catch (SQLException e) {
			Log.e(getClass().getName(), "Unable to reset dives", e);
		}
	}
	
	public Dao<DiveLocationLog, Long> getDiveDao() throws SQLException {
		if (diveDao == null) {
			diveDao = getDao(DiveLocationLog.class);
		}
		return diveDao;
	}
}
