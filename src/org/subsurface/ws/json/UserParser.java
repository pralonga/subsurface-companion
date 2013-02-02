package org.subsurface.ws.json;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.json.JSONException;
import org.json.JSONObject;

public class UserParser {

	public static String parseUser(InputStream in) throws JSONException, IOException {
		// Get stream content
		ByteArrayOutputStream streamContent = new ByteArrayOutputStream();
		byte[] buff = new byte[1024];
		int readBytes;
		while ((readBytes = in.read(buff)) != -1) {
			streamContent.write(buff, 0, readBytes);
		}

		// Parse user
		JSONObject jsonRoot = new JSONObject(new String(streamContent.toByteArray()));
		return jsonRoot.getString("user");
	}
}
