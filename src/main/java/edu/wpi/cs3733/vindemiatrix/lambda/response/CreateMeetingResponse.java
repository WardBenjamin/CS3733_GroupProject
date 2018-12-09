package edu.wpi.cs3733.vindemiatrix.lambda.response;

public class CreateMeetingResponse {
	public final int httpCode;
	
	public final int id;
	public final String name;
	public final String secret_code;
	
	public CreateMeetingResponse(int id, String name, String secret_code, int httpCode) {
		this.httpCode = httpCode;
		this.id = id; 
		this.name = name;
		this.secret_code = secret_code;
	}
	
	public CreateMeetingResponse(int httpCode) {		
		this.id = 0;
		this.name = "";
		this.secret_code = "";
		this.httpCode = httpCode;
	}
	
	public String toString() {
		if (name == null) { return "NoMeeting"; }
		
		return "Meeting(" + id + "," + name + "," + secret_code + ")";
	}
}
