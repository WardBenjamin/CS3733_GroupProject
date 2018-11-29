package edu.wpi.cs3733.vindemiatrix.db.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import edu.wpi.cs.heineman.model.Constant;

public class TimeSlotDAO {
	java.sql.Connection conn; 
	
	pubic TimeSlotDAO{
		ty{
			conn = SchedulerDataBase.connect();
		} catch (Exception e) {
			conn = null; 
		}
	}
	
	/*
	 * search for available time slots
	 * within the input search range 
	 * @param id schedule id  
	 * @param startDate ealiest date of time slots to look for 
	 * @param endDate latest date of time slot to look for 
	 * @param startTime earliest time of time slots to look for
	 * @param endTime latest time of time slots to look for 
	 */
	public List<TimeSlot> getAvailableTimeSlot(Int id, String startDate, String endDate, String startTime, String endTime) throws Exception{
			List<TimeSlot> availableTimeSlots = new ArrayList<>();
			try {
				Statement statement = conn.createStatement();
				String query = "SELECT * FROM TimeSlots";
				ResultSet resultSet = statement.executeQuery(query);

				while (resultSet.next()) {
					TimeSlot t = (resultSet);
					availableTimeSlots.add(c);
				}
				resultSet.close();
				statement.close();
				return allConstants;

			} catch (Exception e) {
				throw new Exception("Failed in getting Available time slots: " + e.getMessage());
			}
		}
	
	/*
	 * update a time slot ot open or closed
	 * @param secretCode secret code for schedule
	 * @param id time slot id 
	 * @param open state to set it to 
	 * (open or closed being true or false respectively)
	 */
	//TODO: updateTimeSlot
	public boolean updteTimeSlot(String secretCode, Int id, Boolean open) {
		try {
			PreparedStatement ps = conn.prepareStatement("UPDATE FROM TimeSlot (secretCode, id, open) values(?,?,?);");
			ps.setString(1, secretCode);
			ps.setInt(2, id);
			ps.setBoolean(3, open);
			int numAltered = ps.executeUpdate();
			ps.close();
			return (numAltered ==1);			
		}catch (Exception e) {
			throw new Exception("Failed to update time slot: " + e.getMessage());
		}
		
	}
	
	private Constant generateTimeSlot(ResultSet resultSet) throws Exception {
		Int id  = resultSet.Int("id");
		String startDate = resultSet.getString("startDate");
		String endDate = resultSet.getString("endDate");
		String startTime = resultSet.getString("startTime");
		String endTime = resultSet.getString("endTime");
		return new Constant (name, value);
	}
}
