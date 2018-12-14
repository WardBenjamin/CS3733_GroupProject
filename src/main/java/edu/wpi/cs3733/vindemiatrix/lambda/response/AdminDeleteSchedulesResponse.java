package edu.wpi.cs3733.vindemiatrix.lambda.response;

public class AdminDeleteSchedulesResponse {
	public final int httpCode;
	public final int num_deleted;
	public final String error;
	
	public AdminDeleteSchedulesResponse(int num_deleted, int httpCode) {
		this.num_deleted = num_deleted;
		this.httpCode = httpCode;
		this.error = "";
	}
	
	public AdminDeleteSchedulesResponse(int httpCode, String error) {
		this.num_deleted = 0;
		this.httpCode = httpCode;
		this.error = error;
	}
	
	public AdminDeleteSchedulesResponse(int httpCode) {
		this.num_deleted = 0;
		this.httpCode = httpCode;
		this.error = "";
	}

}
