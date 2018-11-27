package edu.wpi.cs3733.vindemiatrix.model;

public class Meeting {
	public final int id;
	public final String participant;
	
	/**
	 * Create a meeting object to represent a meeting in a time slot.
	 * @param id The meeting ID
	 * @param participant The secret code for the participant to gain access
	 */
	public Meeting(int id, String participant) {
		this.id = id;
		this.participant = participant;
	}
}
