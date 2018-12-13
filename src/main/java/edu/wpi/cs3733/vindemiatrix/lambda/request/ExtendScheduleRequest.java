package edu.wpi.cs3733.vindemiatrix.lambda.request;

public class ExtendScheduleRequest {
	public final int id;
	public final String start_date;
	public final String end_date;
	public final String secret_code;
	
	public ExtendScheduleRequest(int id, String secret_code, String start_date, String end_date) {
		this.id = id;
		this.secret_code = secret_code;
		this.start_date = start_date;
		this.end_date = end_date;
	}
	
	public boolean isMissingFields() {
		return (id == 0) || (secret_code == null) || (start_date == null) || (end_date == null);
	}
	
	public String toString() {
		return "ExtendSchedule(" + id + "," + secret_code + "," + start_date + "," + end_date + ")";
	}
}
