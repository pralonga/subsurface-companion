package org.subsurface.ws;

import org.subsurface.R;

/**
 * Server error centralization.
 * @author Aurelien PRALONG
 *
 */
public class WsException extends Exception {

	private static final long serialVersionUID = 1L;

	public static final int CODE_UNKNOWN = R.string.error_generic;
	public static final int CODE_BAD_HTTP_CODE = R.string.error_bad_http_code;
	public static final int CODE_LOGIN_INVALID = R.string.error_login_invalid;
	public static final int CODE_EMAIL_ALREADY_EXISTS = R.string.error_email_already_exists;
	public static final int CODE_EMAIL_UNKNOWN = R.string.error_email_unknown;
	public static final int CODE_EMAIL_ID_NOT_MATCHING = R.string.error_email_id_not_matching;
	public static final int CODE_INVALID_REQUEST = R.string.error_invalid_request;
	public static final int CODE_NETWORK_ERROR = R.string.error_network;
	public static final int CODE_PARSE_ERROR = R.string.error_parse_error;

	private final int errorCode;

	public WsException() {
		super();
		this.errorCode = CODE_UNKNOWN;
	}

	public WsException(int code) {
		super(Integer.toString(code));
		this.errorCode = code;
	}

	public WsException(String codeMessage) {
		super(codeMessage);
		if ("Login invalid".equalsIgnoreCase(codeMessage)) {
			this.errorCode = CODE_LOGIN_INVALID;
		} else if ("Email already exists".equalsIgnoreCase(codeMessage)) {
			this.errorCode = CODE_EMAIL_ALREADY_EXISTS;
		} else if ("Email unknown".equalsIgnoreCase(codeMessage)) {
			this.errorCode = CODE_EMAIL_UNKNOWN;
		} else if ("Diver ID and Email do not match".equalsIgnoreCase(codeMessage)) {
			this.errorCode = CODE_EMAIL_ID_NOT_MATCHING;
		} else if ("Invalid request".equalsIgnoreCase(codeMessage)) {
			this.errorCode = CODE_INVALID_REQUEST;
		} else {
			this.errorCode = CODE_UNKNOWN;
		}
	}

	public WsException(Throwable cause) {
		super(cause);
		this.errorCode = CODE_UNKNOWN;
	}

	public int getCode() {
		return errorCode;
	}
}
