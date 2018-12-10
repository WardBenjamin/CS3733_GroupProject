package edu.wpi.cs3733.vindemiatrix.model;

import java.util.ArrayList;
import java.util.Iterator;

public class Schedule {
	public final int id;
	public final String secret_code;
	public final String name;
	public final String start_date;
	public final String end_date;
	public final String start_time;
	public final String end_time;
	public final int meeting_duration;
	
	ArrayList<TimeSlot> timeSlots;
	
	/**
	 * A schedule object to represent schedules in the scheduler.
	 * @param id The schedule ID
	 * @param secret_code The secret code for the organizer
	 * @param name The organizer's name
	 * @param startDate The start date of the schedule
	 * @param endDate The end date of the schedule
	 * @param startTime The start time of the schedule
	 * @param endTime The end time of the schedule
	 */
	public Schedule(int id, String secret_code, String name, String startDate, String endDate, 
			String startTime, String endTime, int meetingDuration) {
		this.id = id;
		this.secret_code = secret_code;
		this.name = name;
		this.start_date = startDate;
		this.end_date = endDate;
		this.start_time = startTime;
		this.end_time = endTime;
		this.meeting_duration = meetingDuration;
	}
	
	/**
	 * Append a time slot from the time slot association table.
	 * @param id The time slot ID
	 * @param date The date of the slot
	 * @param startTime The start time of the slot
	 * @param endTime The end time of the slot
	 * @param isOpen If the slot is open
	 * @param meetingID The meeting ID for the slot or 0 if not set
	 */
	public void appendTimeSlot(int id, String date, String startTime, String endTime, boolean isOpen, int meetingID) {
		timeSlots.add(new TimeSlot(id, date, startTime, endTime, isOpen, null));
	}
	
	/**
	 * Append a time slot from the time slot association table.
	 * @param timeSlot TimeSlot object
	 */
	public void appendTimeSlot(TimeSlot timeSlot) {
		timeSlots.add(timeSlot);
	}
	
	/**
	 * Get an iterator of the time slots for this schedule.
	 * @return Iterator of time slots
	 */
	public Iterator<TimeSlot> timeSlots() {
		return timeSlots.iterator();
	}
}
