package SqlConnector;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
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

	public boolean addManagerDiscount(String[] msg) {
		Statement stm;
		try {
			PreparedStatement ps = conn.prepareStatement(
					"insert into project.managerdiscounts (parkName, startDate, lastDate, precentage, status) values (?, ?, ?, ?, ?)");
			// msg = [startDate , lastDate , precentage , parkName]
			LocalDate start = LocalDate.parse(msg[0]);
			Date startDate = java.sql.Date.valueOf(start);

			LocalDate end = LocalDate.parse(msg[1]);
			Date endDate = java.sql.Date.valueOf(end);

			ps.setString(1, msg[3]);// set park name
			ps.setDate(2, startDate);// set start Date
			ps.setDate(3, endDate);// set end Date
			ps.setFloat(4, Float.valueOf(msg[2]));// set precentage (casting)
			ps.setString(5, "F"); // set status , will be changed by D_M to T if approved

			ps.executeUpdate();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
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
		int i = 0;
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
		i = i / 9;
		return i++;
	}

	public String getManagerDiscount(String[] msg) {
		/* works only for one row in DB */

		Statement stm;
		try {
			PreparedStatement ps = conn.prepareStatement("SELECT * FROM project.managerdiscounts WHERE parkName=? ");
			// msg = [parkName, dateOfVisit]
			ps.setString(1, msg[0]);// parkName
			stm = conn.createStatement();
			ResultSet rs = ps.executeQuery();
			rs.next();
			String discount = "";
			discount += rs.getString(1);// get parkName
			discount += " ";// add space
			discount += rs.getString(2);// get startDate
			discount += " ";// add space
			discount += rs.getDate(3);// get lastDate
			discount += " ";// add space
			discount += rs.getFloat(4);// get precentage
			discount += " ";// add space
			discount += rs.getString(5);// get precentage

			return discount;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}

	}

	public String findOrderToWaitFor(String[] result) {
		// result = [parkName[0],dateOfVisit[1],hour[2],numOfVistors[3],orderNum[4]]
		// SELECT orderNUm FROM project.order where wantedPark = 'testPark' and
		// numOfVisitors >=1 and DateOfVisit = '2020-01-01' and (TimeInPark > '10:00:00'
		// and TimeInPark <= '13:00:00') and status = 'confirmed' ;
		Statement stm;
		try {
			/* get park maxDuration time */
			PreparedStatement ps = conn.prepareStatement("SELECT maxDuration FROM project.park where ParkName = ?;");
			ps.setString(1, result[0]);// parkName
			stm = conn.createStatement();
			ResultSet rs = ps.executeQuery();
			rs.next();
			Time maxDuration = rs.getTime(1);

			Time hour = java.sql.Time.valueOf(result[2]);// hour timeWantedVisit
			Time before_visit = new Time(hour.getTime() - maxDuration.getTime()); // before_visit = hour - maxDuration

			/* get orderNum to wait for */
			ps = conn.prepareStatement(
					"SELECT orderNum FROM project.order where wantedPark = ? and numOfVisitors >=? and DateOfVisit = ? and (TimeInPark > ? and TimeInPark <= ?) and status = ?");
			stm = conn.createStatement();

			ps.setString(1, result[0]);// parkName
			ps.setString(2, result[3]);// numOfVisitors
			ps.setString(3, result[1]);// dateOfVisit
			ps.setString(4, before_visit.toString());// hour - maxDuration
			ps.setString(5, result[2]);// hour
			ps.setString(6, "confirmed");

			String orderNUms = "";

			while (rs.next()) {
				orderNUms += rs.getInt(1);
				orderNUms += " ";
			}

			return orderNUms;// splited by space

		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}

	}

	public boolean addToWaitingList(String result) {
		String msg[] = result.split(" ");
		//msg = [orderToAdd[0] , ordersToWaitFor...]
		Statement stm;
		int cnt,i;
		int numOf_ordersToWaitFor = msg.length - 1 ; //minus 1 because msg[0] = orderToAdd
		try {
			for(i=1;i<numOf_ordersToWaitFor;i++) {
			cnt = 0;
			PreparedStatement ps = conn.prepareStatement("SELECT * FROM project.waitinglist where orderToWaitFor = ?");
			stm = conn.createStatement();
			ps.setString(1, msg[i]);//orderToWaitFor
			ResultSet rs = ps.executeQuery();
			while(rs.next())
				cnt++; // count how many orders alredy wait for this orderToWaitFor
			
			/*enter orderToAdd to waitinglist*/
			ps = conn.prepareStatement("insert into project.waitinglist (orderToWaitFor, waitingOrder, placeInLine) values (?, ?, ?)");
			ps.setString(1, msg[i]);//orderToWaitFor
			ps.setString(2, msg[0]);//orderToAdd
			ps.setString(3, String.valueOf(cnt));//index
			ps.executeUpdate();
			}//for
			
			return true;
			
			
		
		} catch (SQLException e) {
		e.printStackTrace();
		return false;
		}
		
		
		
		
	}

	public String findOrderFirstInLine(String canceldOrderNum) {
		Statement stm;
		int orderNum_firstInLine = -1;
		int minIndex = Integer.MAX_VALUE;
		try {
			PreparedStatement ps = conn.prepareStatement(
					"SELECT watingOrder,placeInLine FROM project.waitinglist WHERE orderToWaitFor=? ");
			ps.setString(1, canceldOrderNum);
			stm = conn.createStatement();
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				if (rs.getInt(2) < minIndex)// placeInLine
					orderNum_firstInLine = rs.getInt(1);// watingOrder
			}

			if (orderNum_firstInLine == -1) // there is no watingOrder for canceldOrderNum
				return "null";

			/* get orderNum_firstInLine details */
			ps = conn.prepareStatement("SELECT * FROM project.order WHERE orderNum=?");
			ps.setString(1, String.valueOf(orderNum_firstInLine));
			stm = conn.createStatement();
			rs = ps.executeQuery();
			String orderToSend = "";
			rs.next();
			orderToSend += rs.getString(1);// ID
			orderToSend += " ";
			orderToSend += rs.getString(2);// type
			orderToSend += " ";
			orderToSend += rs.getString(3);// numOfVisitors
			orderToSend += " ";
			orderToSend += rs.getInt(4);// orderNum
			orderToSend += " ";
			orderToSend += rs.getTime(5);// timeInPark
			orderToSend += " ";
			orderToSend += rs.getDate(6);// DateOfVisit
			orderToSend += " ";
			orderToSend += rs.getString(4);// wantedPark
			orderToSend += " ";
			orderToSend += rs.getFloat(4);// totalPrice
			orderToSend += " ";
			orderToSend += rs.getString(4);// status

			return orderToSend;

		} catch (SQLException e) {
			e.printStackTrace();
			return "null";
		}

	}

	public boolean removeFromWaitingList(String orderToRemove) {
		/* find all rows of orderToRemove */
		Statement stm;
		String orderToWaitFor = "";
		String orderToWaitFor_arr[];
		int i;
		try {
			PreparedStatement ps = conn
					.prepareStatement("SELECT orderToWaitFor FROM project.waitinglist WHERE watingOrder=?");
			ps.setString(1, orderToRemove);
			stm = conn.createStatement();
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				orderToWaitFor += rs.getInt(1);
				orderToWaitFor += " ";
			}

			orderToWaitFor_arr = orderToWaitFor.split(" ");

			/*
			 * To Do: for each orderToWaitFor_arr[i] : remove(orderToRemove) , decrease
			 * placeInLine of all watingOrders for orderToWaitFor_arr[i]
			 */

			for (i = 0; i < orderToWaitFor_arr.length; i++) {
				/* remove */
				ps = conn.prepareStatement("DELETE FROM project.waitinglist WHERE orderToWaitFor=? and watingOrder=?");
				ps.setString(1, orderToWaitFor_arr[i]);
				ps.setString(2, orderToRemove);
				ps.executeUpdate();
				
				/*decrease placeInLine of all watingOrders*/
				ps = conn.prepareStatement("SELECT * FROM project.waitinglist where orderToWaitFor = ?"); //get all remain watingOrders to orderToWaitFor
				stm = conn.createStatement();
				ps.setString(1, orderToWaitFor_arr[i]);//orderToWaitFor
				rs = ps.executeQuery();
				while(rs.next())
				{
				/*get details*/
					int waitingOrder = rs.getInt(2); //waitingOrder
					int oldPlaceInLine = rs.getInt(3);//placeInLine
					int newPlace = oldPlaceInLine-1;
				/*update placeInLine*/
					ps = conn.prepareStatement("UPDATE project.waitinglist SET placeInLine=? where orderToWaitFor = ? and waitingOrder = ?");
					ps.setString(1, String.valueOf(newPlace));//placeInLine
					ps.setString(2, orderToWaitFor_arr[i]);//orderToWaitFor
					ps.setString(3,String.valueOf(waitingOrder));
				}
			}//for
			return true;

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

	}

	public void removeAllwaiters(String orderToRemove_AllWaiters) {
		Statement stm;
		try {
			PreparedStatement ps = conn.prepareStatement("DELETE FROM project.waitinglist WHERE orderToWaitFor=?");
			stm = conn.createStatement();
			ps.setString(1, orderToRemove_AllWaiters);
			ps.executeUpdate();
		

		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
		
	
}//class
