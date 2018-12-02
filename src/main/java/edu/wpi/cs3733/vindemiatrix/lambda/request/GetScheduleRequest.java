package edu.wpi.cs3733.vindemiatrix.lambda.request;

public class GetScheduleRequest {
	public final int id;
	public final String week_start_date;

	/**
	 * Get a request data object with the start/end dates and start/end times. 
	 * @param week_start_date Week Start Date
	 * @param id Schedule ID
	 */
	public GetScheduleRequest( int id, String week_start_date) {
		this.id = id;
		this.week_start_date = week_start_date;
	}
	
	public boolean isMissingFields() {
		return (week_start_date == null) || (id == 0);
	}
	
	public String toString() {
		return "GetSchedule(" + id + "," + week_start_date + ")";
	}
}
