package edu.wpi.cs3733.vindemiatrix.lambda.request;

public class DeleteScheduleRequest {
	public final int id;
	public final String secret_code;
	
	/**
	 * Request for deleting schedules
	 * @param id schedule ID
	 * @param secret_code schedule secret code
	 */
	public DeleteScheduleRequest(int id, String secret_code) {
		this.id = id;
		this.secret_code = secret_code;
	}
	
	public boolean isMissingFields() {
		return (id == 0) || (secret_code == null);
	}
	
	public String toString() {
		return "DeleteSchedule(" + id + "," + secret_code + ")";
	}
}
