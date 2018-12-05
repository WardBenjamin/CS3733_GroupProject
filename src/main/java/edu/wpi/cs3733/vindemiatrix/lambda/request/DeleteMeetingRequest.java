package edu.wpi.cs3733.vindemiatrix.lambda.request;

public class DeleteMeetingRequest{
	public final String secretCode; 
	public final int timeSlotId; 

	/**
	 * Create a request data object with the userName and timeSlotId
	 * @param sc secret code
	 * @param id Time slot id 
	 */
	public DeleteMeetingRequest(String secretCode, int timeSlotId) {
		this.secretCode = secretCode; 
		this.id = id; 
	}
	/*
	 * checks to see if fields are missing 
	 * 
	 */
	public boolean isMissingFields() {
		return (secretCode == null) || (id == 0) ;
	}
	
	public String toString() {
		return "DeleteMeeting(" + secretCode + "," + id + ")";
	}
}
