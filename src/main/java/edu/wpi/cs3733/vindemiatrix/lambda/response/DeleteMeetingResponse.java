package edu.wpi.cs3733.vindemiatrix.lambda.response;

import edu.wpi.cs3733.vindemiatrix.lambda.request.CreateMeetingRequest;
import edu.wpi.cs3733.vindemiatrix.model.Meeting;

public class DeleteMeetingResponse {
	public final int httpCode;
	
	public final String secretCode;
	public final int id;
	
	public DeleteMeetingResponse(DeleteMeetingRequest req, String secretCode, int id,  int httpCode) {
		this.httpCode = httpCode;
		
		this.secretCode = secretCode; 
		this.id = id; 
	}
	
	public DeleteMeetingResponse(int httpCode) {		
		this.id = 0;
		this.secretCode = "" ; 
		this.httpCode = httpCode;
	}
	
	public String toString() {
		if (participant == null) { return "NoName"; }
		
		return "Meeting(" + id + ")";
	}
}
