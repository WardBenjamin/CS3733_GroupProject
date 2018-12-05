package edu.wpi.cs3733.vindemiatrix.lambda.request;

public class CreateScheduleRequest {
	public final String start_date;
	public final String end_date;
	public final String start_time;
	public final String end_time;
	public final int meeting_duration;
	public final int default_open;

	/**
	 * Create a request data object with the start/end dates and start/end times. 
	 * @param sd Start Date
	 * @param ed End Date
	 * @param st Start Time
	 * @param et End Time
	 */
	public CreateScheduleRequest(String start_date, String end_date, 
			String start_time, String end_time, int meeting_duration, int default_open) {
		this.start_date = start_date;
		this.end_date = end_date;
		this.start_time = start_time;
		this.end_time = end_time;
		this.meeting_duration = meeting_duration;
		this.default_open = default_open;
	}
	
	public boolean isMissingFields() {
		return (start_date == null) || (end_date == null) || 
				(start_time == null) || (end_time == null) || (meeting_duration == 0);
	}
	
	public String toString() {
		return "CreateSchedule(" + start_date + "," + end_date + "," + 
								start_time + "," + end_time + "," + 
								meeting_duration + "," + default_open + ")";
	}
}
