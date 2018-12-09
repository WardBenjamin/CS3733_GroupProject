package edu.wpi.cs3733.vindemiatrix.lambda.request;

public class CreateMeetingRequest{
	public final String name; 
	public final int time_slot_id; 

	/**
	 * Create a request data object with the name and timeSlotId
	 * @param name user name 
	 * @param time_slot_id id of the time slot
	 */
	public CreateMeetingRequest(String name, int time_slot_id) {
		this.name = name; 
		this.time_slot_id = time_slot_id; 
	}
	
	public boolean isMissingFields() {
		return (name == null) || (time_slot_id == 0) ;
	}
	
	public String toString() {
		return "CreateMeeting(" + name + "," + time_slot_id + ")";
	}
}
