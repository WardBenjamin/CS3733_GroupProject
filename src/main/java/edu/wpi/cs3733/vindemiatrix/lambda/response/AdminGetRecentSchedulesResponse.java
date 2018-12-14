package edu.wpi.cs3733.vindemiatrix.lambda.response;

import java.util.Iterator;
import java.util.List;

import edu.wpi.cs3733.vindemiatrix.model.Schedule;

public class AdminGetRecentSchedulesResponse {
	public final int httpCode;
	public final String error;
	
	public final Schedule schedules[];
	
	public AdminGetRecentSchedulesResponse(List<Schedule> schedules, int httpCode) {
		this.error = "";
		this.httpCode = httpCode;
		this.schedules = new Schedule[schedules.size()];
		int x = 0;
		
		for (Iterator<Schedule> i = schedules.iterator(); i.hasNext();) {
			this.schedules[x++] = i.next();
		}
	}
	
	public AdminGetRecentSchedulesResponse(int httpCode, String error) {
		this.schedules = null;
		this.httpCode = httpCode;
		this.error = error;
	}
	
	public AdminGetRecentSchedulesResponse(int httpCode) {
		this.error = "";
		this.schedules = null;
		this.httpCode = httpCode;
	}

}
