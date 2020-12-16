package SqlConnector;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;


public class sqlConnector {

	private Connection conn;

	public sqlConnector(Connection conn) {
		this.conn = conn;
	}

	public String[] CheckForId(String msg) {
		Statement stm;
		String[] s = new String[5];
		try {
			PreparedStatement ps = conn.prepareStatement("SELECT * FROM project.person WHERE ID=?");
			stm = conn.createStatement();
			ps.setString(1, msg);
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				s[0] = rs.getString(1);
				s[1] = rs.getString(2);
				s[2] = rs.getString(3);
				s[3] = rs.getString(4);
				s[4] = rs.getString(5);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return s;
	}
	/*
	 * 
	 * This method will get a string with the email and the id to be changed will
	 * return true or false
	 * 
	 * 
	 */

	public boolean updateEmail(String[] msg) {
		Statement stm;
		try {
			PreparedStatement ps = conn.prepareStatement("UPDATE project.person SET email=? WHERE ID=?");
			ps.setString(1, msg[1]);
			ps.setString(2, msg[0]);
			ps.executeUpdate();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}

	}

	public boolean exists(String[] msg) {
		Statement stm;
		try {
			PreparedStatement ps = conn.prepareStatement("SELECT * FROM project.person WHERE ID=?");
			stm = conn.createStatement();
			ps.setString(1, msg[0]);
			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				return false;
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return true;
	}

	public boolean addMember(String[] msg) {
		Statement stm;
		String memberCNT = String.valueOf(nextMember());
		try {
			PreparedStatement ps = conn.prepareStatement(
					"INSERT project.person SET ID=? firstName=? lastName=? phoneNumber=? Email=? creditCardNum=? maxFamilyMembers=? memberId=?");
			ps.setString(1, msg[0]);
			ps.setString(2, msg[1]);
			ps.setString(3, msg[2]);
			ps.setString(4, msg[3]);
			ps.setString(5, msg[4]);
			ps.setString(6, msg[5]);
			ps.setString(7, msg[6]);
			ps.setString(8, memberCNT);

			ps.executeUpdate();
			System.out.println("opale");
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		

	}
	
	public int nextMember() {
		Statement stm;
		int i=0;
	try {
		PreparedStatement ps = conn.prepareStatement("SELECT * FROM project.person");
		stm = conn.createStatement();
		ResultSet rs = ps.executeQuery();

		while (rs.next()) {
			i++;
		}

	} catch (SQLException e) {
		e.printStackTrace();
	}
	
	return i;
}
	/*
	 * will check the amount of orders that is in the system
	 * 
	 */
	public int nextOrder() {
		Statement stm;
		int i=0;
	try {
		PreparedStatement ps = conn.prepareStatement("SELECT * FROM gonaturedb.order");
		stm = conn.createStatement();
		ResultSet rs = ps.executeQuery();

		while (rs.next()) {
			i++;
		}

	} catch (SQLException e) {
		e.printStackTrace();
	}
	
	return ++i;
	}
	
	/*
	 * 
	 * check how many visitors in total there is for the desired time and date and return this value
	 * 
	 */
	public int howManyForCurrentTimeAndDat1e(String[] msg) {
		String res;
		int counter=0;
		Statement stm;
		try {
			PreparedStatement ps = conn.prepareStatement("SELECT numOfVisitors FROM project.order WHERE TimeInPark=? AND DateOfVisit=? AND wantedPark=?");
			ps.setString(1, msg[0]);
			ps.setString(2, msg[1]);
			ps.setString(3, msg[2]);
			ResultSet rs=ps.executeQuery();
			while(rs.next()) {
				res=rs.getString(1);
				counter+=Integer.parseInt(res);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			
		}
		return counter;
		
	}
	
	/*
	 * method that will check how many visitors allowed to be in wanted park in total.
	 */
	public int howManyAllowedInPark(String parkName) {
		String res;
		int counter=0;
		Statement stm;
		try {
			PreparedStatement ps = conn.prepareStatement("SELECT maxAvailableVisitors FROM gonaturedb.park WHERE ParkName=?");
			ps.setString(1, parkName);
			
			ResultSet rs=ps.executeQuery();
			rs.next();
			
			res=rs.getString(1);
			counter+=Integer.parseInt(res);
			
		} catch (SQLException e) {
			e.printStackTrace();
			
		}
		return counter;
	}
	
	

	public int howManyForCurrentTimeAndDate(String[] result) throws ParseException {
		Statement stm;
		LocalDate wanted1 = LocalDate.parse(result[3]);
		Date wanted=java.sql.Date.valueOf(wanted1);
		
		int counter=0;
		try {
			PreparedStatement ps = conn.prepareStatement("SELECT numOfVisitors,orderNum FROM gonaturedb.order WHERE wantedPark=? AND DateOfVisit=? AND TimeInPark BETWEEN ? AND ? ");
			ps.setString(1, result[2]);
			ps.setString(3, result[0]);
			ps.setString(4, result[1]);
			ps.setString(2, result[3]);
			ResultSet rs=ps.executeQuery();
			
			while(rs.next()) {
				counter+=rs.getInt(1);
				System.out.println(rs.getString(2));
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
			
		}
		return counter;
	}

	/*Method to insert new order to table
	 * res[0]=time res[1]=date, res[2]= parkname , res[3]=price, res[4]=id, res[5]=type, res[6]=numOfVisit
	 * 
	 */
	
	public void addOrder(int orderNum, String[] result) {
		Statement stm;
		LocalDate wanted1 = LocalDate.parse(result[1]);
		Date wanted=java.sql.Date.valueOf(wanted1);
		Time visitTime =java.sql.Time.valueOf(result[0]);
		
		
		try {
			PreparedStatement ps = conn.prepareStatement("INSERT INTO gonaturedb.order (orderNum, TimeInPark, DateOfVisit, wantedPark, TotalPrice, ID,type,numOfVisitors) VALUES (?,?,?,?,?,?,?,?)");
			ps.setInt(1, orderNum);
			ps.setTime(2, visitTime);
			ps.setDate(3, wanted);
			ps.setString(4, result[2]);
			ps.setFloat(5, Float.parseFloat(result[3]));
			ps.setString(6, result[4]);
			ps.setString(7, result[5]);
			ps.setString(8, result[6]);
			
			ps.execute();
		} catch (SQLException e) {
			e.printStackTrace();
			
		}
		
		
	}
	
	/*
	 * this method will search for the desired order and delete it from the DB.
	 * res[0]=time, res[1]=date, res[2]=wantedPark, res[3]=id
	 */
	

	public void cancelOrder(String[] result) {
		Statement stm;

		try {
			PreparedStatement ps = conn.prepareStatement("DELETE FROM gonaturedb.order WHERE TimeInPark=? AND DateOfVisit=? AND wantedPark=? AND ID=?");
			ps.setString(1, result[0]);
			ps.setString(2, result[1]);
			ps.setString(3, result[2]);
			ps.setString(4, result[3]);
			ps.execute();
		} catch (SQLException e) {
			e.printStackTrace();
			
		}
		
		
		
		
	}

	public String getOrders(String iD) {
		Statement stm;
		StringBuffer s= new StringBuffer();
		try {
			PreparedStatement ps = conn.prepareStatement("SELECT orderNum,DateOfVisit,wantedPark,TimeInPark,numOfVisitors,TotalPrice FROM gonaturedb.order WHERE ID=?");
			ps.setString(1, iD);
			ResultSet rs=ps.executeQuery();
			while(rs.next()) {
				for(int i=1;i<=6;i++)
				{
					s.append(rs.getString(i));
					s.append(" ");
				}
				
			}
		} catch (SQLException e) {
			e.printStackTrace();
			
		}
		s.append("Done");
		
		return s.toString();
	}
	
}
