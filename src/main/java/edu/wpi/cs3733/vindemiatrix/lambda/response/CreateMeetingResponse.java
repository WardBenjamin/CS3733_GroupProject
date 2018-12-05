package edu.wpi.cs3733.vindemiatrix.lambda.response;

import edu.wpi.cs3733.vindemiatrix.lambda.request.CreateMeetingRequest;
import edu.wpi.cs3733.vindemiatrix.model.Meeting;

public class CreateMeetingResponse {
	public final int httpCode;
	
	public final int id;
	public final String participant;
	
	public CreateScheduleResponse(CreateMeetingRequest req, int id, String participant, int httpCode) {
		this.req = req;
		this.httpCode = httpCode;
		
		this.id = id; 
		this.participant = participant;
	}
	
	public CreateMeetingResponse(int httpCode) {		
		this.id = 0;
		this.participant = "";
		this.httpCode = httpCode;
	}
	
	public String toString() {
		if (participant == null) { return "NoParticipant"; }
		
		return "Meeting(" + participant + ")";
	}
}
