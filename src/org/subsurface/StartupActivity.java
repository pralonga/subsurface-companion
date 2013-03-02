package org.subsurface;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;

/**
 * Startup activity. Does the switch between account actions and dive list.
 * @author Aurelien PRALONG
 *
 */
public class StartupActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		String user = PreferenceManager.getDefaultSharedPreferences(this).getString("user_id", null);
		if (user == null || user.trim().isEmpty()) {
			startActivity(new Intent(this, AccountLinkActivity.class));
		} else {
			startActivity(new Intent(this, HomeActivity.class));
		}
		finish();
	}
}
