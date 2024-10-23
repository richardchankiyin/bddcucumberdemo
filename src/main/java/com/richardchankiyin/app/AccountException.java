package com.richardchankiyin.app;

public class AccountException extends RuntimeException {

	private static final long serialVersionUID = 5631622998266036368L;

	public AccountException(String msg) {
		super(msg);
	}
	
	public AccountException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
