package edu.wpi.cs3733.vindemiatrix.lambda.response;

import edu.wpi.cs3733.vindemiatrix.lambda.request.CreateMeetingRequest;
import edu.wpi.cs3733.vindemiatrix.model.Meeting;

public class CreateMeetingResponse {
	public final int httpCode;
	
	public final int id;
	public final String name;
	
	public CreateScheduleResponse(CreateMeetingRequest req, int id, String name, int httpCode) {
		this.req = req;
		this.httpCode = httpCode;
		
		this.id = id; 
		this.name = name;
	}
	
	public CreateMeetingResponse(int httpCode) {		
		this.id = 0;
		this.name = "";
		this.httpCode = httpCode;
	}
	
	public String toString() {
		if (name == null) { return "Noname"; }
		
		return "Meeting(" + name + ")";
	}
}
