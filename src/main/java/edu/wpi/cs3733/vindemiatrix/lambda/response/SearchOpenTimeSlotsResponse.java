package edu.wpi.cs3733.vindemiatrix.lambda.response;

import java.util.Iterator;
import java.util.List;

import edu.wpi.cs3733.vindemiatrix.model.TimeSlot;

public class SearchOpenTimeSlotsResponse {
	public final int httpCode;
	public final String error;
	
	public final TimeSlot time_slots[];
	
	public SearchOpenTimeSlotsResponse(List<TimeSlot> ts, int httpCode) {
		this.error = "";
		this.httpCode = httpCode;
		this.time_slots = new TimeSlot[ts.size()];
		int x = 0;
		
		for (Iterator<TimeSlot> i = ts.iterator(); i.hasNext();) {
			time_slots[x++] = i.next();
		}
	}
	
	public SearchOpenTimeSlotsResponse(int httpCode, String error) {
		this.time_slots = null;
		this.httpCode = httpCode;
		this.error = error;
	}
	
	public SearchOpenTimeSlotsResponse(int httpCode) {
		this.error = "";
		this.time_slots = null;
		this.httpCode = httpCode;
	}
	
}
