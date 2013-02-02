package org.subsurface.controller;

import java.util.ArrayList;
import java.util.List;

import org.subsurface.dao.DbAdapter;
import org.subsurface.dao.DiveLocationLogDao;
import org.subsurface.model.DiveLocationLog;
import org.subsurface.ws.WsClient;
import org.subsurface.ws.WsClientStub;
import org.subsurface.ws.WsException;

import android.content.Context;

public class DiveController {

	public static DiveController instance = new DiveController();

	private final WsClient wsClient = new WsClientStub();
	private DiveLocationLogDao diveDao;
	private final List<DiveLocationLog> dives;
	private boolean loaded = false;

	private DiveController() {
		this.dives = new ArrayList<DiveLocationLog>();
	}

	public void setContext(Context context) {
		if (diveDao != null) {
			diveDao.close();
		}
		if (context != null) {
			this.diveDao = new DbAdapter(context).getDiveLocationLogDao();
			diveDao.open();
		}
	}

	public List<DiveLocationLog> getDiveLogs() {
		if (!loaded) {
			dives.clear();
			dives.addAll(diveDao.getAllDiveLocationLogs());
			loaded = true;
		}
		return dives;
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

	public void addDiveLog(DiveLocationLog diveLog) {
		if (diveDao.find(diveLog.getTimestamp()) == null && diveLog.getId() == 0) {
			diveDao.save(diveLog);
			loaded = false;
		}
	}

	public void updateDiveLog(DiveLocationLog diveLog) {
		// TODO
	}

	public void sendDiveLog(DiveLocationLog diveLog) throws WsException {
		try {
			wsClient.postDive(diveLog,
					UserController.instance.getBaseUrl(),
					UserController.instance.getUser());
			diveLog.setSent(true);
		} finally {
			diveDao.save(diveLog);
		}
	}

	public void deleteDiveLog(DiveLocationLog diveLog) {
		diveDao.deleteDiveLocationLog(diveLog);
		loaded = false;
	}

	public void deleteAll() {
		diveDao.deleteAll();
		loaded = false;
	}

	public void startUpdate() throws WsException {
		List<DiveLocationLog> dives = wsClient.getAllDives(
				UserController.instance.getBaseUrl(),
				UserController.instance.getUser());
		for (DiveLocationLog dive : dives) {
			addDiveLog(dive);
		}
	}
}
