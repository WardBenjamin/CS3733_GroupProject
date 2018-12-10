package edu.wpi.cs3733.vindemiatrix.db.dao;

import java.sql.*; 
//import java.text.DecimalFormat;
import java.util.ArrayList; 
import java.util.Calendar; 
import java.util.List;
import java.util.UUID;

import edu.wpi.cs3733.vindemiatrix.db.SchedulerDatabase;
import edu.wpi.cs3733.vindemiatrix.model.Meeting;

import java.util.GregorianCalendar; 

public class MeetingDAO {
	Connection conn; 
	
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
	 * @param name user that is making the meeting
	 */
	public Meeting createMeeting(int timeSlotId, String name) throws Exception {
		try {
			Meeting m = null; 
			PreparedStatement ps = conn.prepareStatement("INSERT INTO `meetings` (`secret_code`, `name`) VALUES (?,?);");
			
			UUID uuid = UUID.randomUUID();
			ps.setString(1, uuid.toString());
			ps.setString(2, name);
			ps.execute();
			
			ps = conn.prepareStatement("SELECT * FROM `meetings` WHERE `secret_code` = ?;");
			ps.setString(1, uuid.toString());
			ResultSet rs = ps.executeQuery();
			
			if (rs.next()) {
				m = new Meeting(rs.getInt(1), uuid.toString(), name);
			}
			
			ps.close();
			rs.close();
			return m;
		} catch (Exception e) {
			throw new Exception("Failed to create meeting: " + e.getMessage());
		}
	}
	
	/*
	 * deletes a meeting 
	 * @param secretCode secret code for the meeting
	 * @param id meeting id 
	 */
	public boolean deleteMeeting(String secretCode, int id) throws Exception{
		try {
			PreparedStatement ps = conn.prepareStatement("DELETE FROM `meetings` WHERE `secret_code` = ? AND `id` = ?;");
			ps.setString(1, secretCode);
			ps.setInt(2, id);
			int numAltered = ps.executeUpdate();
			ps.close();
			return (numAltered == 1);
		} catch (Exception e) {
			throw new Exception("Failed to delete meeting: " + e.getMessage());
		}
	}
}
