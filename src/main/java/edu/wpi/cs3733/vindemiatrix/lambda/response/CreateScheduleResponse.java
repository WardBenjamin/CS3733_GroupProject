package edu.wpi.cs3733.vindemiatrix.lambda.response;

import edu.wpi.cs3733.vindemiatrix.model.Schedule;

public class CreateScheduleResponse {
	String id;
	int httpCode;

	public CreateScheduleResponse(String id, int httpCode) {
		this.id = id;
		this.httpCode = httpCode;
	}
	
	public CreateScheduleResponse(int httpCode) {
		this.id = null;
		this.httpCode = httpCode;
	}
	
	public CreateScheduleResponse(String id) {
		this.id = id;
		this.httpCode = 200;
	}
	
	public String toString() {
		if (id == null) { return "NoSchedule"; }
		
		return "Schedule(" + id + ")";
	}
}
