package edu.wpi.cs3733.vindemiatrix.model;

public class TimeSlot {
	public final int id;
	public final String date;
	public final String startTime;
	public final String endTime;
	public final boolean isOpen;
	public final Meeting meeting;
	
	/**
	 * Create a time slot object to represent a time slot in a schedule.
	 * @param id The time slot ID
	 * @param date The date of the time slot
	 * @param startTime The start time of the time slot
	 * @param endTime The end time of the time slot
	 * @param isOpen If the time slot is open
	 * @param meeting The meeting in this time slot, or NULL if no meeting
	 */
	public TimeSlot(int id, String date, String startTime, String endTime, 
			boolean isOpen, Meeting meeting) {
		this.id = id;
		this.date = date;
		this.startTime = startTime;
		this.endTime = endTime;
		this.isOpen = isOpen;
		this.meeting = meeting;
	}
}
