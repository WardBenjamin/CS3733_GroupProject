package edu.wpi.cs3733.vindemiatrix.lambda.request;

public class SearchOpenTimeSlotsRequest {
	public final int schedule_id;
	public final int year;
	public final int month;
	public final int day_of_week;
	public final int day_of_month;
	public final String time_slot;
	
	public SearchOpenTimeSlotsRequest(int schedule_id, int year, int month, int day_of_week, int day_of_month, String time_slot) {
		this.schedule_id = schedule_id;
		this.year = year;
		this.month = month;
		this.day_of_week = day_of_week;
		this.day_of_month = day_of_month;
		this.time_slot = time_slot;
	}
	
	public boolean isMissingFields() {
		return (schedule_id == 0);
	}
	
	public String toString() {
		return "SearchOpenTimeSlot(" + schedule_id + "," + year + "," + month + "," + day_of_week + "," + 
									day_of_month + "," + time_slot + ")";
	}
}
