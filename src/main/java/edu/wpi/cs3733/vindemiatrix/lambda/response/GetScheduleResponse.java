package edu.wpi.cs3733.vindemiatrix.lambda.response;

import java.util.Iterator;
import java.util.List;

import edu.wpi.cs3733.vindemiatrix.model.Schedule;
import edu.wpi.cs3733.vindemiatrix.model.TimeSlot;

public class GetScheduleResponse {
	public final int httpCode;
	
	public final Schedule schedule;
	public final TimeSlot time_slots[];
	
	public GetScheduleResponse(Schedule s, List<TimeSlot> ts, int httpCode) {
		this.schedule = s;
		this.httpCode = httpCode;
		this.time_slots = new TimeSlot[ts.size()];
		int x = 0;
		
		for (Iterator<TimeSlot> i = ts.iterator(); i.hasNext();) {
			time_slots[x++] = i.next();
		}
	}
	
	public GetScheduleResponse(int httpCode) {
		this.schedule = null;
		this.time_slots = null;
		this.httpCode = httpCode;
	}
}
