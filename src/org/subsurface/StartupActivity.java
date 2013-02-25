package org.subsurface;

import org.subsurface.controller.DiveController;
import org.subsurface.controller.UserController;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

/**
 * Startup activity. Does the switch between account actions and dive list.
 * @author Aurelien PRALONG
 *
 */
public class StartupActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		UserController.instance.setContext(this);
		try {
			DiveController.instance.setContext(this);
		} catch (Exception e) {
			new AlertDialog.Builder(this)
					.setTitle(R.string.error_title)
					.setMessage(R.string.error_fatal)
					.setPositiveButton(android.R.string.ok,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									finish();
								}
							}).setCancelable(false).show();
		}

		if (UserController.instance.getUser() == null || UserController.instance.getUser().length() == 0) {
			startActivity(new Intent(this, AccountLinkActivity.class));
		} else {
			startActivity(new Intent(this, HomeActivity.class));
		}
		finish();
	}
}
