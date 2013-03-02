package org.subsurface.ws.json;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Parser for server errors.
 * @author Aurelien PRALONG
 *
 */
public class ErrorParser {

	public static String parseErrorCode(InputStream in) throws JSONException, IOException {
		ByteArrayOutputStream streamContent = new ByteArrayOutputStream();
		byte[] buff = new byte[1024];
		int readBytes;
		while ((readBytes = in.read(buff)) != -1) {
			streamContent.write(buff, 0, readBytes);
		}
		
		// Parse error
		JSONObject jsonRoot = new JSONObject(new String(streamContent.toByteArray()));
		return jsonRoot.optString("error");
	}
}
