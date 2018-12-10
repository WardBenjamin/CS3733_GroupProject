package edu.wpi.cs3733.vindemiatrix.lambda.response;

public class BasicResponse {
	public final int httpCode;
	public final String error;
	
	public BasicResponse(int httpCode, String error) {
		this.httpCode = httpCode;
		this.error = error;
	}
	
	public BasicResponse(int httpCode) {
		this.httpCode = httpCode;
		this.error = "";
	}
}
