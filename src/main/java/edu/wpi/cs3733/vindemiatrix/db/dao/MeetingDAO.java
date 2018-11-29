package edu.wpi.cs3733.vindemiatrix.db.dao;

import java.sql.*; 
//import java.text.DecimalFormat;
import java.util.ArrayList; 
import java.util.Calendar; 
import java.util.List;

import edu.wpi.cs3733.vindemiatrix.db.SchedulerDatabase;
import edu.wpi.cs3733.vindemiatrix.model.Meeting;

import java.util.GregorianCalendar; 

public class MeetingDAO {
	java.sql.Connection conn; 
	
	public MeetingDAO() {
		try {
			conn = SchedulerDatabase.connect(); 
		} catch (Exception e) {
			conn = null;
		}
	}
	
	/*
	 * creates a meeting
	 * @param timeSlotID the time slot to create the meeting within
	 * @param usrName user that is making the meeting
	 */
	public Boolean createMeeting(int timeSlotId, String usrName) throws Exception {
		try {
			Meeting m = null; 
			PreparedStatement ps = conn.prepareStatement("SELECT * FROM `meetings` WHERE name = ?;");
			ps.setNString(1, timeSlotId);
			ResultSet resultSet ps.executeQuery(); 
			ps = conn.prepareStatement("INSERT INTO Meetings (timSlotId, usrName) values(?,?);");
			ps.setInt(1, timeSlotId);
			ps.setString(2, usrName);
			ps.execute();
			resultSet.close();
			ps.close();

			if (constant == null) {
				throw new Exception("Constant not found");
			} 
		}
		catch (Exception e) {
			throw new Exception("Failed to insert schedule: " + e.getMessage());
		}
	}
	
	/*
	 * deletes a meeting 
	 * @param secretCode secret code for the meeting
	 * @param id meeting id 
	 */
	public boolean deleteMeeting(String secretCode, int id) throws Exception{
		try {
			PreparedStatement ps = conn.prepareStatement("DELETE FROM Meetings (secretCode, id) values(?,?);");
			ps.setString(1, secretCode);
			ps.setInt(2,  id);
			int numAltered = ps.executeUpdate();
			ps.close();
			return (numAltered ==1);			
		}catch (Exception e) {
			throw new Exception("Failed to delete meeting: " + e.getMessage());
		}
	}
	private Meeting generateMeeting(ResultSet resultSet) throws Exception {
		int timeSlotId  = resultSet.getInt("timeSlotId");
		String usrName = resultSet.getString("usrName");
		return new Meeting (timeSlotId, usrName);
	}

}
