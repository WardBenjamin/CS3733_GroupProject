package edu.wpi.cs3733.vindemiatrix.lambda.request;

public class CreateMeetingRequest{
	public final String name; 
	public final int id; 

	/**
	 * Create a request data object with the name and timeSlotId
	 * @param un user name 
	 * @param idts id of the time slot
	 */
	public CreateMeetingRequest(String name, int id) {
		this.name = name; 
		this.id = id; 
	}
	
	public boolean isMissingFields() {
		return (name == null) || (id == 0) ;
	}
	
	public String toString() {
		return "CreateMeeting(" + name + "," + id + ")";
	}
}
