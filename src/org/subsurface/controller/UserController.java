package org.subsurface.controller;

import org.subsurface.R;

import android.content.Context;
import android.preference.PreferenceManager;

/**
 * Controller for user / user settings.
 * @author Aurelien PRALONG
 *
 */
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
		return PreferenceManager.getDefaultSharedPreferences(context).getString("destination_url", context.getString(R.string.default_url));
	}

	public boolean syncOnstartup() {
		return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("sync_on_startup", true);
	}

	public boolean autoSend() {
		return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("auto_send", true);
	}
}
