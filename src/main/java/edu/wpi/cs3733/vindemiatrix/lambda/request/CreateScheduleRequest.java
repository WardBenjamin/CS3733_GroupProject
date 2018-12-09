package edu.wpi.cs3733.vindemiatrix.lambda.request;

public class CreateScheduleRequest {
	public final String name;
	public final String start_date;
	public final String end_date;
	public final String start_time;
	public final String end_time;
	public final int meeting_duration;
	public final int default_open;

	/**
	 * Create a request data object with the start/end dates and start/end times. 
	 * @param name the organizer's name
	 * @param start_date Start Date
	 * @param end_date End Date
	 * @param start_time Start Time
	 * @param end_time End Time
	 * @param meeting_duration the meeting duration
	 * @param default_open whether to default time slots to open or closed
	 */
	public CreateScheduleRequest(String name, String start_date, String end_date, 
			String start_time, String end_time, int meeting_duration, int default_open) {
		this.name = name;
		this.start_date = start_date;
		this.end_date = end_date;
		this.start_time = start_time;
		this.end_time = end_time;
		this.meeting_duration = meeting_duration;
		this.default_open = default_open;
	}
	
	public boolean isMissingFields() {
		return (name == null) || (start_date == null) || (end_date == null) || 
				(start_time == null) || (end_time == null) || (meeting_duration == 0);
	}
	
	public String toString() {
		return "CreateSchedule(" + name + "," + start_date + "," + end_date + "," + 
								start_time + "," + end_time + "," + 
								meeting_duration + "," + default_open + ")";
	}
}
