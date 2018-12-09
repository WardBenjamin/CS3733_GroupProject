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
	
	/**
	 * Get the time slots with the schedule ID and a date range
	 * @param id Schedule ID
	 * @param start_of_week start of the week
	 * @param end_of_week end of the week
	 * @return a List of time slots
	 * @throws Exception on SQL failure
	 */
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
						m = new Meeting(meeting_rs.getInt(1), meeting_rs.getString(3), "");
					}
				}
				
				timeSlots.add(new TimeSlot(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getBoolean(5), m));
			}
		} catch (Exception e) {
			throw new Exception("Failed to get time slots: " + e.getMessage());
		}
		
		return timeSlots;
	}
	
	/**
	 * Set a time slot's meeting ID to a meeting
	 * @param time_slot_id the time slot
	 * @param meeting_id the meeting
	 * @return true on success, false on failure
	 * @throws Exception on SQL failure
	 */
	public boolean setTimeSlotMeeting(int time_slot_id, int meeting_id) throws Exception {

		try {
			PreparedStatement ps = conn.prepareStatement("UPDATE `time_slots` SET `meeting` = ? WHERE `id` = ?;");
				
			ps.setInt(1,  meeting_id);
			
			if (time_slot_id == 0) {
				ps.setNull(2, java.sql.Types.INTEGER);
			} else {
				ps.setInt(2, time_slot_id);
			}
			
			int changed = ps.executeUpdate();
			ps.close();
			
			return changed == 1;
		} catch (Exception e) {
			throw new Exception("Failed to update time slot meeting id: " + e.getMessage());
		}
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
	
	/**
	 * Create a time slot with given parameters
	 * @param schedule_id the schedule ID for this slot
	 * @param date the date for the time slot
	 * @param start_time the start time
	 * @param end_time the end time
	 * @param default_open whether to have the slot open or closed
	 * @return TimeSlot object
	 * @throws Exception on SQL failure of insertion/creation
	 */
	public TimeSlot createTimeSlot(int schedule_id, String date, String start_time, String end_time, int default_open) throws Exception {
		TimeSlot ts = null;
		
		try {
			PreparedStatement ps = conn.prepareStatement(
				"INSERT INTO `time_slots` (`schedule_id`, `date`, `start_time`, `end_time`, `is_open`) VALUES (?,?,?,?,?);");
			
			ps.setInt(1, schedule_id);
			ps.setString(2, date);
			ps.setString(3, start_time);
			ps.setString(4, end_time);
			ps.setInt(5, default_open);
			ps.execute();
			
			ps = conn.prepareStatement("SELECT `id` FROM `time_slots` WHERE `schedule_id` = ? AND `date` = ? AND `start_time` = ? AND `end_time` = ?");
			ps.setInt(1, schedule_id);
			ps.setString(2, date);
			ps.setString(3, start_time);
			ps.setString(4, end_time);

			if (ps.execute()) {
				ResultSet rs = ps.getResultSet();

				int id = 0;
				if (rs.first()) { id = rs.getInt(1); }
				
				ts = new TimeSlot(id, date, start_time, end_time, default_open == 1, null);
			}

			ps.close();
		} catch (Exception e) {
			throw new Exception("Exception while creating time slot: " + e.getMessage());
		}
		
		return ts;
	}
	
	/**
	 * Delete all the time slots and the meetings they contain for a given schedule
	 * @note no need to validate secret code as this is only (and should only) be called after schedule deletion, which checks secret code
	 * @param schedule_id the schedule ID
	 * @return true of success, false on failure
	 * @exception Exception on SQL failure
	 */
	public boolean deleteTimeSlots(int schedule_id) throws Exception {
		boolean success = true;
		
		try {
			PreparedStatement ps = conn.prepareStatement("SELECT * FROM `time_slots` WHERE `schedule_id` = ?;");
			
			ps.setInt(1, schedule_id);
			ResultSet rs = ps.executeQuery();
			
			while (rs.next()) {
				int meeting_id = 0;
				if ((meeting_id = rs.getInt(7)) != 0) {
					ps = conn.prepareStatement("DELETE FROM `meetings` WHERE `id` = ?");
					ps.setInt(1, meeting_id);
					ps.execute();
				}
			}
			
			ps = conn.prepareStatement("DELETE FROM `time_slots` WHERE `schedule_id` = ?");
			ps.setInt(1,  schedule_id);
			success = ps.executeUpdate() != 0;
			
			ps.close();
		} catch (Exception e) {
			throw new Exception("Exception while deleting time slots: " + e.getMessage());
		}
		
		return success;
	}
}
