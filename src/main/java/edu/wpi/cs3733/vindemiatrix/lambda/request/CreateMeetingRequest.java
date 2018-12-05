package edu.wpi.cs3733.vindemiatrix.lambda.request;

public class CreateMeetingRequest{
	public final String userName; 
	public final int id; 

	/**
	 * Create a request data object with the userName and timeSlotId
	 * @param un user name 
	 * @param idts id of the time slot
	 */
	public CreateMeetingRequest(String userName, int id) {
		this.userName = userName; 
		this.id = id; 
	}
	
	public boolean isMissingFields() {
		return (userName == null) || (id == 0) ;
	}
	
	public String toString() {
		return "CreateMeeting(" + userName + "," + id + ")";
	}
}
