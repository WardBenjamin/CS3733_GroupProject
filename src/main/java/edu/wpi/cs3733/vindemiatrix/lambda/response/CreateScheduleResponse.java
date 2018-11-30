package edu.wpi.cs3733.vindemiatrix.lambda.response;

import edu.wpi.cs3733.vindemiatrix.lambda.request.CreateScheduleRequest;
import edu.wpi.cs3733.vindemiatrix.model.Schedule;

public class CreateScheduleResponse {
	public final String organizer;
	public final int httpCode;
	
	public final String start_date;
	public final String end_date;
	public final String start_time;
	public final String end_time;
	public final int meeting_duration;
	
	public final int days;
	public final int timeSlotsPerDay;

	public CreateScheduleResponse(String organizer, CreateScheduleRequest req, int days, int timeSlotsPerDay, int httpCode) {
		this.organizer = organizer;
		this.httpCode = httpCode;
		
		this.start_date = req.start_date;
		this.end_date = req.end_date;
		this.start_time = req.start_time;
		this.end_time = req.end_time;
		this.meeting_duration = req.meeting_duration;
		this.days = days;
		this.timeSlotsPerDay = timeSlotsPerDay;
	}
	
	public CreateScheduleResponse(int httpCode) {
		this.organizer = null;
		
		this.start_date = "";
		this.end_date = "";
		this.start_time = "";
		this.end_time = "";
		this.meeting_duration = 0;
		this.days = 0;
		this.timeSlotsPerDay = 0;
		
		this.httpCode = httpCode;
	}
	
	public String toString() {
		if (organizer == null) { return "NoSchedule"; }
		
		return "Schedule(" + organizer + ")";
	}
}
