package org.magnum.mobilecloud.video.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value=HttpStatus.BAD_REQUEST, reason="Video Bad Request")  // 400
public class VideoBadRequestException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2426951869362086393L;

}
