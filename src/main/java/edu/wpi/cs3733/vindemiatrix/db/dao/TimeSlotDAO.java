package edu.wpi.cs3733.vindemiatrix.db.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
		ArrayList<TimeSlot> timeSlots = new ArrayList<>();
		
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
				
				if ((mid = rs.getInt(7)) != 0) {
					PreparedStatement meeting_query = conn.prepareStatement("SELECT * FROM `meetings` WHERE `id` = ?");
					meeting_query.setInt(1, mid);
					ResultSet meeting_rs = meeting_query.executeQuery();
					if (meeting_rs.next()) {
						m = new Meeting(meeting_rs.getInt(1), "", meeting_rs.getString(3));
					}
				}
				
				timeSlots.add(new TimeSlot(rs.getInt(1), rs.getString(3), rs.getString(4), rs.getString(5), rs.getBoolean(6), m));
			}
		} catch (Exception e) {
			throw new Exception("Failed to get time slots: " + e.getMessage());
		}
		
		return timeSlots;
	}
	
	/**
	 * Check if a time slot has a meeting
	 * @param time_slot_id the time slot
	 * @return true if has a meeting, false otherwise
	 * @throws Exception on SQL failure
	 */
	public boolean timeSlotHasMeeting(int time_slot_id) throws Exception {
		int meeting = 0;
		try {
			PreparedStatement ps = conn.prepareStatement("SELECT * FROM `time_slots` WHERE `id` = ?;");
			ps.setInt(1, time_slot_id);
			ResultSet rs = ps.executeQuery();
			
			if (rs.next()) {
				meeting = rs.getInt(7);
			}
			
			ps.close();
			rs.close();
		} catch (Exception e) {
			throw new Exception("Failed to check time slot meeting id: " + e.getMessage());
		}
		
		return meeting != 0;
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

			ps.setInt(2, time_slot_id);
			
			if (meeting_id == 0) {
				ps.setNull(1, java.sql.Types.INTEGER);
			} else {
				ps.setInt(1,  meeting_id);
			}
			
			int changed = ps.executeUpdate();
			ps.close();
			
			return changed == 1;
		} catch (Exception e) {
			throw new Exception("Failed to update time slot meeting id: " + e.getMessage());
		}
	}

	/**
	 * Find open time slots
	 * @param schedule_id the schedule to look in
	 * @param date_query a string compatible with SQL LIKE syntax
	 * @param time a string compatible with SQL LIKE syntax
	 * @param day_of_week the day of week or -1
	 * @return available time slots
	 * @throws Exception
	 */
	public List<TimeSlot> findOpenTimeSlots(int schedule_id, String date_query, String time, int day_of_week) throws Exception{
		List<TimeSlot> availableTimeSlots = new ArrayList<>();
		try {
			PreparedStatement ps = conn.prepareStatement("SELECT * FROM `time_slots` WHERE `schedule_id` = ? AND `is_open` = 1 AND `meeting` IS NULL AND `date` LIKE ? AND `start_time` LIKE ?;");
			ps.setInt(1, schedule_id);
			ps.setString(2, date_query);
			ps.setString(3, time);
			ResultSet rs = ps.executeQuery();
			
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			Calendar c = Calendar.getInstance();
			
			while (rs.next()) {
				if (day_of_week == -1) {
					availableTimeSlots.add(new TimeSlot(rs.getInt("id"), rs.getString("date"), rs.getString("start_time"), 
							rs.getString("end_time"), true, null));
				} else {
					// take day of week into consideration
					c.setTime(dateFormat.parse(rs.getString("date")));
					if (c.get(Calendar.DAY_OF_WEEK) == day_of_week) {
						availableTimeSlots.add(new TimeSlot(rs.getInt("id"), rs.getString("date"), rs.getString("start_time"), 
								rs.getString("end_time"), true, null));
					}
				}
			}

			return availableTimeSlots;
		} catch (Exception e) {
			throw new Exception("Failed in getting Available time slots: " + e.getMessage());
		}
	}
	
	/**
	 * update a time slot to open or closed
	 * @param id time slot id 
	 * @param open state to set it to 
	 * (open or closed being true or false respectively)
	 */
	public boolean updateTimeSlot(int id,int open) throws Exception {
		try {
			PreparedStatement ps = conn.prepareStatement("UPDATE `time_slots` SET `is_open` = ? WHERE `id` = ?;");
			ps.setInt(1, open);
			ps.setInt(2, id);
			int numAltered = ps.executeUpdate();
			ps.close();
			return (numAltered == 1);			
		} catch (Exception e) {
			throw new Exception("Failed to update time slot: " + e.getMessage());
		}	
	}
	
	/**
	 * check if there is a time slot on a day
	 * @param day the day (YYYY-MM-DD)
	 * @param schedule_id schedule id 
	 */
	public boolean hasMeetingOnDay(String day, int schedule_id) throws Exception {
		try {
			PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) AS conflicts FROM `time_slots` WHERE `schedule_id` = ? AND `date` = ? AND `meeting` IS NOT NULL;");
			ps.setInt(1, schedule_id);
			ps.setString(2, day);
			ResultSet rs = ps.executeQuery();
			int count = 0;
			if (rs.next()) { count = rs.getInt("conflicts"); }
			ps.close();
			return (count > 0);
		} catch (Exception e) {
			throw new Exception("Failed to check for time slots on a specified day: " + e.getMessage());
		}	
	}
	
	/**
	 * update time slots to open or closed on a day for schedule
	 * @param day the day (YYYY-MM-DD)
	 * @param schedule_id schedule id 
	 * @param open state to set it to 
	 * (open or closed being true or false respectively)
	 */
	public boolean updateTimeSlotsOnDay(String day, int schedule_id, boolean open) throws Exception {
		try {
			PreparedStatement ps = conn.prepareStatement("UPDATE `time_slots` SET `is_open` = ? WHERE `schedule_id` = ? AND `date` = ?;");
			ps.setBoolean(1, open);
			ps.setInt(2, schedule_id);
			ps.setString(3, day);
			int numAltered = ps.executeUpdate();
			ps.close();
			return (numAltered > 0);
		} catch (Exception e) {
			throw new Exception("Failed to update time slots on a specified day: " + e.getMessage());
		}	
	}
	
	/**
	 * check if any meetings are in a specific time slot across all days
	 * @param timeslot the time it starts at
	 * @param schedule_id schedule id 
	 */
	public boolean hasMeetingAtTimeSlot(String timeslot, int schedule_id) throws Exception {
		try {
			PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) AS conflicts FROM `time_slots` WHERE `schedule_id` = ? AND `start_time` = ? AND `meeting` IS NOT NULL;");
			ps.setInt(1, schedule_id);
			ps.setString(2, timeslot);
			ResultSet rs = ps.executeQuery();
			int count = 0;
			if (rs.next()) { count = rs.getInt("conflicts"); }
			ps.close();
			return (count > 0);		
		} catch (Exception e) {
			throw new Exception("Failed to update time slot: " + e.getMessage());
		}	
	}
	
	/**
	 * update a time slot to open or closed on each hour in a schedule
	 * @param timeslot the time it starts at
	 * @param schedule_id schedule id 
	 * @param open state to set it to 
	 * (open or closed being true or false respectively)
	 */
	public boolean updateTimeSlotOnHours(String timeslot, int schedule_id, boolean open) throws Exception {
		try {
			PreparedStatement ps = conn.prepareStatement("UPDATE `time_slots` SET `is_open` = ? WHERE `schedule_id` = ? AND `start_time` = ?;");
			ps.setBoolean(1, open);
			ps.setInt(2, schedule_id);
			ps.setString(3, timeslot);
			int numAltered = ps.executeUpdate();
			ps.close();
			return (numAltered > 0);			
		} catch (Exception e) {
			throw new Exception("Failed to update time slot: " + e.getMessage());
		}	
	}
	
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
	
	/**
	 * Delete all the time slots and the meetings they contain for a given set of schedules
	 * @param schedule_ids the schedule IDs
	 * @return num deleted
	 * @exception Exception on SQL failure
	 */
	public int adminDeleteTimeSlots(String schedule_ids) throws Exception {
		int num_deleted = 0;
		
		try {
			// this is safe from injection as WE generate schedule_ids from the database
			PreparedStatement ps = conn.prepareStatement("SELECT * FROM `time_slots` WHERE `schedule_id` IN (" + schedule_ids + ");");
			ResultSet rs = ps.executeQuery();
			
			while (rs.next()) {
				int meeting_id = 0;
				if ((meeting_id = rs.getInt(7)) != 0) {
					ps = conn.prepareStatement("DELETE FROM `meetings` WHERE `id` = ?");
					ps.setInt(1, meeting_id);
					ps.execute();
				}
			}
			
			ps = conn.prepareStatement("DELETE FROM `time_slots` WHERE `schedule_id` IN (" + schedule_ids + ")");
			num_deleted = ps.executeUpdate();
			ps.close();
		} catch (Exception e) {
			throw new Exception("Exception while deleting time slots: " + e.getMessage());
		}
		
		return num_deleted;
	}
	
	/**
	 * check if the secret code given is the code for the given time slot
	 * @param id the time slot ID
	 * @param secretCode the schedule secret code
	 * @return 0 if good, 1 if invalid form, 2 if incorrect code
	 * @throws Exception on SQL failure
	 */
	public int isAuthorized(int id, String secretCode) throws Exception {
		Pattern p = Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");
		Matcher m = p.matcher(secretCode);
		
		if (!m.find()) { return 1; }
		
		try {
			PreparedStatement ps = conn.prepareStatement("SELECT * FROM `time_slots` WHERE `id` = ?;");
			ps.setInt(1, id);
			ResultSet rs = ps.executeQuery();
			
			if (rs.next()) {
				int schedule_id = rs.getInt(2);
				ps = conn.prepareStatement("SELECT * FROM `schedules` WHERE `id` = ?;");
				ps.setInt(1, schedule_id);
				rs.close();
				rs = ps.executeQuery();
				
				if (rs.next()) {
					System.out.println(rs.getString(2));
					System.out.println(secretCode);
					if (rs.getString(2).equals(secretCode)) {
						rs.close();
						ps.close();
						return 0;
					}
				}
			}
			
			ps.close();
			return 2;
		} catch (Exception e) {
			throw new Exception("Failed to check meeting authorization: " + e.getMessage());
		}
	}
}
