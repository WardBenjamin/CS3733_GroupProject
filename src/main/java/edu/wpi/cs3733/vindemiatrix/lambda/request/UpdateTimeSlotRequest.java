package edu.wpi.cs3733.vindemiatrix.lambda.request;

public class UpdateTimeSlotRequest {
	public final String mode;
	public final int schedule_id;
	public final int time_slot_id;
	public final int hour;
	public final String day;
	public final int open;
	
	public UpdateTimeSlotRequest(String mode, int schedule_id, int time_slot_id, int hour, String day, int open) {
		this.mode = mode;
		this.schedule_id = schedule_id;
		this.time_slot_id = time_slot_id;
		this.hour = hour;
		this.day = day;
		this.open = open;
	}
	
	public boolean isMissingFields() {
		if (mode == "indiv") {
			return (time_slot_id == 0);
		} else if (mode == "day") {
			return (day == "" || schedule_id == 0);
		} else if (mode == "hour") {
			return (hour == 0 || schedule_id == 0);
		} else { return true; }
	}
	
	public String toString() {
		return "UpdateTimeSlot(" + mode + "," + schedule_id + "," + time_slot_id + "," + 
				hour + "," + day + "," + open + ")";
	}
}
