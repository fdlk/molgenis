package org.molgenis.modelviz;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value=HttpStatus.INTERNAL_SERVER_ERROR, reason="Query parameters are not valid") 
public class IllegalParameterException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3372705140753143116L;
}
