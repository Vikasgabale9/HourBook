package com.app.hourbook.exception;

public class VendorNotFoundException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4424785149762191095L;

	public VendorNotFoundException() {
		// TODO Auto-generated constructor stub
	}

	public VendorNotFoundException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public VendorNotFoundException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public VendorNotFoundException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public VendorNotFoundException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

}
