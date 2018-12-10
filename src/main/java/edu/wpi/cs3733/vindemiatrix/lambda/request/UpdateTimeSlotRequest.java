package edu.wpi.cs3733.vindemiatrix.lambda.request;

public class UpdateTimeSlotRequest {
	public final String mode;
	public final int schedule_id;
	public final int time_slot_id;
	public final String timeslot;
	public final String day;
	public final int open;
	
	public UpdateTimeSlotRequest(String mode, int schedule_id, int time_slot_id, String timeslot, String day, int open) {
		this.mode = mode;
		this.schedule_id = schedule_id;
		this.time_slot_id = time_slot_id;
		this.timeslot = timeslot;
		this.day = day;
		this.open = open;
	}
	
	public boolean isMissingFields() {
		if (mode.equals("indiv")) {
			return (time_slot_id == 0);
		} else if (mode.equals("day")) {
			return (day == "" || schedule_id == 0);
		} else if (mode.equals("slot")) {
			return (timeslot == "" || schedule_id == 0);
		} else { return true; }
	}
	
	public String toString() {
		return "UpdateTimeSlot(" + mode + "," + schedule_id + "," + time_slot_id + "," + 
				timeslot + "," + day + "," + open + ")";
	}
}
