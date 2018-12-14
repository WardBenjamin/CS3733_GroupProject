package edu.wpi.cs3733.vindemiatrix.lambda.request;

public class AdminGetRecentSchedulesRequest {
	public final int hours;
	
	/**
	 * Request for recent schedules
	 * @param hours within last N hours
	 */
	public AdminGetRecentSchedulesRequest(int hours) {
		this.hours = hours;
	}
	
	public boolean isMissingFields() { return false; }
	
	public String toString() {
		return "AdminGetRecentSchedules(" + hours + ")";
	}
}
