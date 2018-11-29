package edu.wpi.cs3733.vindemiatrix.lambda.request;

public class CreateScheduleRequest {
	public final String start_date;
	public final String end_date;
	public final String start_time;
	public final String end_time;

	/**
	 * Create a request data object with the start/end dates and start/end times. 
	 * @param sd Start Date
	 * @param ed End Date
	 * @param st Start Time
	 * @param et End Time
	 */
	public CreateScheduleRequest(String start_date, String end_date, 
			String start_time, String end_time) {
		this.start_date = start_date;
		this.end_date = end_date;
		this.start_time = start_time;
		this.end_time = end_time;
	}
	
	public boolean isMissingFields() {
		return (start_date == null) || (end_date == null) || 
				(start_time == null) || (end_time == null);
	}
	
	public String toString() {
		return "CreateSchedule(" + start_date + "," + end_date + "," + 
								start_time + "," + end_time + "," + ")";
	}
}
