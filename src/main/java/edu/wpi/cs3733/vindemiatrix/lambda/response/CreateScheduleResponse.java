package edu.wpi.cs3733.vindemiatrix.lambda.response;

import edu.wpi.cs3733.vindemiatrix.lambda.request.CreateScheduleRequest;
import edu.wpi.cs3733.vindemiatrix.model.Schedule;

public class CreateScheduleResponse {
	public final String name;
	public final int httpCode;
	public final String error;

	public final int id;
	public final String secret_code;
	public final String start_date;
	public final String end_date;
	public final String start_time;
	public final String end_time;
	public final int meeting_duration;
	
	public final int days;
	public final int timeSlotsPerDay;

	public CreateScheduleResponse(Schedule s, int days, int timeSlotsPerDay, int httpCode) {
		this.name = s.name;
		this.secret_code = s.secret_code;
		this.httpCode = httpCode;
		this.error = "";
		
		this.start_date = s.start_date;
		this.end_date = s.end_date;
		this.start_time = s.start_time;
		this.end_time = s.end_time;
		this.meeting_duration = s.meeting_duration;
		this.id = s.id;
		this.days = days;
		this.timeSlotsPerDay = timeSlotsPerDay;
	}
	
	public CreateScheduleResponse(int httpCode) {
		this.name = "";
		this.secret_code = "";
		this.start_date = "";
		this.end_date = "";
		this.start_time = "";
		this.end_time = "";
		this.meeting_duration = 0;
		this.id = 0;
		this.days = 0;
		this.timeSlotsPerDay = 0;
		
		this.httpCode = httpCode;
		this.error = "";
	}
	
	public CreateScheduleResponse(int httpCode, String error) {
		this.name = "";
		this.secret_code = "";
		this.start_date = "";
		this.end_date = "";
		this.start_time = "";
		this.end_time = "";
		this.meeting_duration = 0;
		this.id = 0;
		this.days = 0;
		this.timeSlotsPerDay = 0;
		
		this.httpCode = httpCode;
		this.error = error;
	}
	
	public String toString() {
		if (name == "") { return "NoSchedule"; }
		
		return "Schedule(" + secret_code + "," + name + "," + id + ")";
	}
}
