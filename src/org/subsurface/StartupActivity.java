package org.subsurface;

import org.subsurface.controller.DiveController;
import org.subsurface.controller.UserController;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class StartupActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		UserController.instance.setContext(this);
		DiveController.instance.setContext(this);

		if (UserController.instance.getUser() == null || UserController.instance.getUser().length() == 0) {
			startActivity(new Intent(this, AccountLinkActivity.class));
		} else {
			startActivity(new Intent(this, HomeActivity.class));
		}
		finish();
	}
}
