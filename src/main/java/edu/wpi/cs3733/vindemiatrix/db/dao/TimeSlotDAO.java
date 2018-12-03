package edu.wpi.cs3733.vindemiatrix.db.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import edu.wpi.cs3733.vindemiatrix.db.SchedulerDatabase;
import edu.wpi.cs3733.vindemiatrix.model.Meeting;
import edu.wpi.cs3733.vindemiatrix.model.Schedule;
import edu.wpi.cs3733.vindemiatrix.model.TimeSlot;

public class TimeSlotDAO {
	Connection conn; 
	
	public TimeSlotDAO() {
		try {
			conn = SchedulerDatabase.connect();
		} catch (Exception e) {
			conn = null; 
		}
	}
	
	public List<TimeSlot> getTimeSlots(int id, String start_of_week, String end_of_week) throws Exception {
		List<TimeSlot> timeSlots = new ArrayList<>();
		
		try {
			PreparedStatement ps = conn.prepareStatement(
					"SELECT * FROM `time_slots` WHERE `schedule_id` = ? AND `date` BETWEEN ? AND ?;");
				
			ps.setInt(1, id);
			ps.setString(2, start_of_week);
			ps.setString(3, end_of_week);
			
			ResultSet rs = ps.executeQuery();
			
			while (rs.next()) {
				int mid = 0;
				Meeting m = null;
				
				if ((mid = rs.getInt(6)) != 0) {
					PreparedStatement meeting_query = conn.prepareStatement("SELECT * FROM `meetings` WHERE `id` = ?");
					meeting_query.setInt(1, mid);
					ResultSet meeting_rs = meeting_query.executeQuery();
					if (meeting_rs.next()) {
						m = new Meeting(meeting_rs.getInt(1), "");
					}
				}
				
				timeSlots.add(new TimeSlot(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getBoolean(5), m));
			}
		} catch (Exception e) {
			throw new Exception("Failed to get time slots: " + e.getMessage());
		}
		
		return timeSlots;
	}
	
	/*
	 * search for available time slots
	 * within the input search range 
	 * @param id schedule id  
	 * @param startDate earliest date of time slots to look for 
	 * @param endDate latest date of time slot to look for 
	 * @param startTime earliest time of time slots to look for
	 * @param endTime latest time of time slots to look for 
	 */
//	public List<TimeSlot> getAvailableTimeSlot(Int id, String startDate, String endDate, String startTime, String endTime) throws Exception{
//			List<TimeSlot> availableTimeSlots = new ArrayList<>();
//			try {
//				Statement statement = conn.createStatement();
//				String query = "SELECT * FROM TimeSlots";
//				ResultSet resultSet = statement.executeQuery(query);
//
//				while (resultSet.next()) {
//					TimeSlot t = (resultSet);
//					availableTimeSlots.add(c);
//				}
//				resultSet.close();
//				statement.close();
//				return allConstants;
//
//			} catch (Exception e) {
//				throw new Exception("Failed in getting Available time slots: " + e.getMessage());
//			}
//		}
	
	/*
	 * update a time slot to open or closed
	 * @param secretCode secret code for schedule
	 * @param id time slot id 
	 * @param open state to set it to 
	 * (open or closed being true or false respectively)
	 */
	//TODO: updateTimeSlot
//	public boolean updteTimeSlot(String secretCode, Int id, Boolean open) {
//		try {
//			PreparedStatement ps = conn.prepareStatement("UPDATE FROM TimeSlot (secretCode, id, open) values(?,?,?);");
//			ps.setString(1, secretCode);
//			ps.setInt(2, id);
//			ps.setBoolean(3, open);
//			int numAltered = ps.executeUpdate();
//			ps.close();
//			return (numAltered ==1);			
//		}catch (Exception e) {
//			throw new Exception("Failed to update time slot: " + e.getMessage());
//		}
//		
//	}
	
//	private Constant generateTimeSlot(ResultSet resultSet) throws Exception {
//		Int id  = resultSet.Int("id");
//		String startDate = resultSet.getString("startDate");
//		String endDate = resultSet.getString("endDate");
//		String startTime = resultSet.getString("startTime");
//		String endTime = resultSet.getString("endTime");
//		return new Constant (name, value);
//	}
	
	public TimeSlot createTimeSlot(int schedule_id, String date, String start_time, String end_time) throws Exception {
		TimeSlot ts = null;
		
		try {
			PreparedStatement ps = conn.prepareStatement(
				"INSERT INTO `time_slots` (`schedule_id`, `date`, `start_time`, `end_time`) VALUES (?,?,?,?);");
			
			ps.setInt(1, schedule_id);
			ps.setString(2, date);
			ps.setString(3, start_time);
			ps.setString(4, end_time);
			ps.execute();
			
			ps = conn.prepareStatement("SELECT `id` FROM `time_slots` WHERE `schedule_id` = ? AND `date` = ? AND `start_time` = ? AND `end_time` = ?");
			ps.setInt(1, schedule_id);
			ps.setString(2, date);
			ps.setString(3, start_time);
			ps.setString(4, end_time);

			if (ps.execute()) {
				ResultSet rs = ps.getResultSet();

				int id = 0;
				if (rs.first()) {
					id = rs.getInt(1);
				}
				
				ts = new TimeSlot(id, date, start_time, end_time, true, null);
			}

			ps.close();
		} catch (Exception e) {
			throw new Exception("Exception while creating time slot: " + e.getMessage());
		}
		
		return ts;
	}
}
