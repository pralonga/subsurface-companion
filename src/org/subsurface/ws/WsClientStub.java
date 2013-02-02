package org.subsurface.ws;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.subsurface.model.DiveLocationLog;

public class WsClientStub extends WsClient {

	@Override
	public List<DiveLocationLog> getAllDives(String url, String user) {
		try {
			Thread.sleep(2000);
		} catch(Exception ignored) {}
		prepareRequest(new HttpGet(), url, ACTION_GET_ALL_DIVES, null, user);
		ArrayList<DiveLocationLog> dives = new ArrayList<DiveLocationLog>();
		Calendar today = Calendar.getInstance();
		int id = 0;
		{
			DiveLocationLog dive = new DiveLocationLog();
			dive.setLatitude(1.2345);
			dive.setLongitude(5.4321);
			dive.setName("Stub dive #" + id);
			today.add(Calendar.DATE, -(++id));
			dive.setTimestamp(today.getTimeInMillis());
			dives.add(dive);
		}
		{
			DiveLocationLog dive = new DiveLocationLog();
			dive.setLatitude(1.2345);
			dive.setLongitude(5.4321);
			dive.setName("Stub dive #" + id);
			today.add(Calendar.DATE, -(++id));
			dive.setTimestamp(today.getTimeInMillis());
			dives.add(dive);
		}
		{
			DiveLocationLog dive = new DiveLocationLog();
			dive.setLatitude(1.2345);
			dive.setLongitude(5.4321);
			dive.setName("Stub dive #" + id);
			today.add(Calendar.DATE, -(++id));
			dive.setTimestamp(today.getTimeInMillis());
			dives.add(dive);
		}
		{
			DiveLocationLog dive = new DiveLocationLog();
			dive.setLatitude(1.2345);
			dive.setLongitude(5.4321);
			dive.setName("Stub dive #" + id);
			today.add(Calendar.DATE, -(++id));
			dive.setTimestamp(today.getTimeInMillis());
			dives.add(dive);
		}
		{
			DiveLocationLog dive = new DiveLocationLog();
			dive.setLatitude(1.2345);
			dive.setLongitude(5.4321);
			dive.setName("Stub dive #" + id);
			today.add(Calendar.DATE, -(++id));
			dive.setTimestamp(today.getTimeInMillis());
			dives.add(dive);
		}
		return dives;
	}

	@Override
	public void postDive(DiveLocationLog dive, String url, String user) throws WsException {
		try {
			Thread.sleep(2000);
		} catch(Exception ignored) {}
		prepareRequest(new HttpPost(), url, ACTION_POST_DIVE, null);
	}

	@Override
	public String createUser(String url, String email) throws WsException {
		try {
			Thread.sleep(2000);
		} catch(Exception ignored) {}
		prepareRequest(new HttpGet(), url, ACTION_CREATE_USER, null, email);
		return "TODO";
	}

	@Override
	public void resendUser(String url, String email) throws WsException {
		try {
			Thread.sleep(2000);
		} catch(Exception ignored) {}
		prepareRequest(new HttpGet(), url, ACTION_RESEND_USER, null, email);
	}
}
