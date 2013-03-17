package org.subsurface.controller;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.subsurface.dao.DatabaseHelper;
import org.subsurface.model.DiveLocationLog;
import org.subsurface.ws.WsClient;
import org.subsurface.ws.WsException;

import android.content.Context;
import android.util.Log;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

/**
 * Controller for dives. Keeps a cache, and manages synchronization with database.
 * @author Aurelien PRALONG
 *
 */
public class DiveController {

	private static final String TAG = "DiveController";
	public static DiveController instance = new DiveController();

	private final WsClient wsClient = new WsClient();
	private DatabaseHelper helper;
	private Dao<DiveLocationLog, Long> diveDao;
	private final List<DiveLocationLog> dives;
	private boolean loaded = false;

	private DiveController() {
		this.dives = new ArrayList<DiveLocationLog>();
	}

	public void setContext(Context context) throws SQLException {
		if (context != null) {
			if (helper == null) {
				helper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
				this.diveDao = helper.getDiveDao();
			}
		} else {
			OpenHelperManager.releaseHelper();
		}
	}

	public DatabaseHelper getHelper() {
		return helper;
	}

	public void forceUpdate() {
		try {
			loaded &= dives.size() == diveDao.queryBuilder()
					.where().eq(DiveLocationLog.KEY_HIDDEN, false)
					.countOf();
		} catch (Exception ignored) {}
	}

	public List<DiveLocationLog> getDiveLogs() {
		try {
			if (!loaded) {
				dives.clear();
				List<DiveLocationLog> dbDives = diveDao.queryBuilder()
						.orderBy(DiveLocationLog.KEY_TIMESTAMP, false)
						.where().eq(DiveLocationLog.KEY_HIDDEN, false)
						.query();
				if (dbDives != null) {
					dives.addAll(dbDives);
				}
				loaded = true;
			}
		} catch (Exception e) {
			Log.d(TAG, "Could not retrieve dives", e);
		}
		return dives;
	}

	public List<DiveLocationLog> getFilteredDives(String name, long startDate, long endDate, boolean pendingOnly) {
		Log.d(TAG, "Searching from " + new Date(startDate) + " to " + new Date(endDate));
		List<DiveLocationLog> allLogs = getDiveLogs();
		ArrayList<DiveLocationLog> filteredLogs = new ArrayList<DiveLocationLog>();
		String lName = name == null ? null : name.toLowerCase();
		for (DiveLocationLog log : allLogs) {
			if ((lName == null || (log.getName() != null && log.getName().toLowerCase().contains(lName)))
					&& startDate <= log.getTimestamp() && log.getTimestamp() <= endDate
					&& (!pendingOnly || log.isSent())) {
				filteredLogs.add(log);
			}
		}
		return filteredLogs;
	}

	public List<DiveLocationLog> getPendingLogs() {
		List<DiveLocationLog> allLogs = getDiveLogs();
		ArrayList<DiveLocationLog> filteredLogs = new ArrayList<DiveLocationLog>();
		for (DiveLocationLog log : allLogs) {
			if (!log.isSent()) {
				filteredLogs.add(log);
			}
		}
		return filteredLogs;
	}

	public DiveLocationLog getDiveById(long id) {
		DiveLocationLog found = null;
		try {
			found = diveDao.queryForId(id);
		} catch (Exception ignored) {}
		return found;
	}

	public void updateDiveLog(DiveLocationLog diveLog) {
		Calendar diveDate = Calendar.getInstance();
		diveDate.setTimeInMillis(diveLog.getTimestamp());
		diveDate.set(Calendar.SECOND, 0);
		diveDate.set(Calendar.MILLISECOND, 0);
		diveLog.setTimestamp(diveDate.getTimeInMillis());
		try {
			diveDao.createOrUpdate(diveLog);
			loaded = false;
		} catch (Exception e) {
			Log.d(TAG, "Could not update dive", e);
		}
	}

	public void sendDiveLog(DiveLocationLog diveLog) throws WsException {
		try {
			wsClient.postDive(diveLog,
					UserController.instance.getBaseUrl(),
					UserController.instance.getUser());
			diveLog.setSent(true);
		} finally {
			updateDiveLog(diveLog);
		}
	}

	public void deleteDiveLog(DiveLocationLog diveLog) throws WsException {
		try {
			if (diveLog.isSent()) {
				wsClient.deleteDive(diveLog,
						UserController.instance.getBaseUrl(),
						UserController.instance.getUser());
			}
			diveDao.delete(diveLog);
			loaded = false;
		} catch (SQLException e) {
			Log.d(TAG, "Could not delete dive", e);
		}
	}

	public void deleteAll() {
		helper.resetDives();
		loaded = false;
	}

	public void startUpdate() throws WsException {
		List<DiveLocationLog> dives = wsClient.getAllDives(
				UserController.instance.getBaseUrl(),
				UserController.instance.getUser());
		for (DiveLocationLog dive : dives) {
			try {
				if (diveDao.queryForEq(DiveLocationLog.KEY_TIMESTAMP, dive.getTimestamp()).size() == 0) {
					updateDiveLog(dive);
				}
			} catch (Exception e) {
				Log.d(TAG, "Could not retrieve dive", e);
			}
		}
	}
}
