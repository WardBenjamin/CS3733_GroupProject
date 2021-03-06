package edu.wpi.cs3733.vindemiatrix.lambda.request;

public class DeleteMeetingRequest{
	public final String secret_code; 
	public final int time_slot_id; 
	public final int meeting_id;

	/**
	 * Create a request data object with the userName and timeSlotId
	 * @param sc secret code
	 * @param time_slot_id Time slot id 
	 */
	public DeleteMeetingRequest(String secret_code, int meeting_id, int time_slot_id) {
		this.secret_code = secret_code; 
		this.meeting_id = meeting_id;
		this.time_slot_id = time_slot_id; 
	}
	/*
	 * checks to see if fields are missing 
	 * 
	 */
	public boolean isMissingFields() {
		return (secret_code == null) || (time_slot_id == 0) || (meeting_id == 0);
	}
	
	public String toString() {
		return "DeleteMeeting(" + secret_code + "," + meeting_id + "," + time_slot_id + ")";
	}
}
