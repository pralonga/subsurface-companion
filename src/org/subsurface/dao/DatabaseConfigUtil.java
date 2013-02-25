package org.subsurface.dao;

import java.io.IOException;
import java.sql.SQLException;

import org.subsurface.model.DiveLocationLog;

import com.j256.ormlite.android.apptools.OrmLiteConfigUtil;

public class DatabaseConfigUtil extends OrmLiteConfigUtil {

	public static void main(String[] args) throws SQLException, IOException {
		writeConfigFile("ormlite_config.txt", new Class[] { DiveLocationLog.class });
	}
}
