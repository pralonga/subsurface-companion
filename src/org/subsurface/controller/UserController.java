package org.subsurface.controller;

import android.content.Context;
import android.preference.PreferenceManager;

public class UserController {

	public static final UserController instance = new UserController();

	private Context context;

	private UserController() {}

	public void setContext(Context context) {
		this.context = context;
	}

	public String getUser() {
		return PreferenceManager.getDefaultSharedPreferences(context).getString("user_id", null);
	}

	public void setUser(String user) {
		PreferenceManager.getDefaultSharedPreferences(context).edit()
				.remove("user_id")
				.putString("user_id", user)
				.commit();
	}

	public String getBaseUrl() {
		return PreferenceManager.getDefaultSharedPreferences(context).getString("destination_url", null);
	}
}
