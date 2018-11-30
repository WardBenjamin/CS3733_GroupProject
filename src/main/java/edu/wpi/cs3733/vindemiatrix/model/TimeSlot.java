package edu.wpi.cs3733.vindemiatrix.model;

import java.sql.Date;
import java.sql.Time;

public class TimeSlot {
	public final int id;
	public final String date;
	public final String startTime;
	public final String endTime;
	public final boolean isOpen;
	public final int meetingID;
	
	/**
	 * Create a time slot object to represent a time slot in a schedule.
	 * @param id The time slot ID
	 * @param date The date of the time slot
	 * @param startTime The start time of the time slot
	 * @param endTime The end time of the time slot
	 * @param isOpen If the time slot is open
	 * @param meetingID The meeting ID for a meeting in this time slot, or 0 if no meeting
	 */
	public TimeSlot(int id, String date, String startTime, String endTime, 
			boolean isOpen, int meetingID) {
		this.id = id;
		this.date = date;
		this.startTime = startTime;
		this.endTime = endTime;
		this.isOpen = isOpen;
		this.meetingID = meetingID;
	}
}
