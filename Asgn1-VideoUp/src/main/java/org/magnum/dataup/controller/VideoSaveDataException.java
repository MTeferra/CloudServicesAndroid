package org.magnum.dataup.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value=HttpStatus.INTERNAL_SERVER_ERROR, reason="Video Save Data Error")  // 500
public class VideoSaveDataException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
