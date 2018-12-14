package edu.wpi.cs3733.vindemiatrix.lambda.request;

public class AdminDeleteSchedulesRequest {
	public final int days;
	
	/**
	 * Request for deleting schedules
	 * @param days the N days old cutoff
	 */
	public AdminDeleteSchedulesRequest(int days) {
		this.days = days;
	}
	
	public boolean isMissingFields() { return false; }
	
	public String toString() {
		return "AdminDeleteSchedules(" + days + ")";
	}
}
