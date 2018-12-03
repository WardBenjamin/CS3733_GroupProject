package edu.wpi.cs3733.vindemiatrix.lambda.request;

public class GetScheduleRequest {
	public final int id;
	public final String week_start_date;

	/**
	 * Get a request data object with the start/end dates and start/end times. 
	 * @param organizer Organizer code
	 * @param week_start_date Week Start Date
	 */
	public GetScheduleRequest(String id, String week_start_date) {
		this.id = Integer.parseInt(id);
		this.week_start_date = week_start_date;
	}
	
	public boolean isMissingFields() {
		return (id == 0) || (week_start_date == null);
	}
	
	public String toString() {
		return "GetSchedule(" + id + "," + week_start_date + ")";
	}
}
