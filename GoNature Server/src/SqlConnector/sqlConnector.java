package SqlConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class sqlConnector {
	
	private Connection conn; 
	
	public sqlConnector(Connection conn) {
		this.conn=conn;
	}
	
	public String[] CheckForId(String msg) {
		Statement stm;
		String[] s = new String[5]; 
		try {
			PreparedStatement ps = conn.prepareStatement("SELECT * FROM project.visitors WHERE ID=?");
			stm = conn.createStatement();
			ps.setString(1, msg);
			ResultSet rs = ps.executeQuery();
			
			while(rs.next())
	 		{
				 s[0]=rs.getString(1);
				 s[1]=rs.getString(2);
				 s[2]=rs.getString(3);
				 s[3]=rs.getString(4);
				 s[4]=rs.getString(5);
			} 
			
			
			
		}catch(SQLException e) {e.printStackTrace();}
		return s;
	}
	/*
	 * 
	 * This method will get a string with the email and the id to be changed
	 * will return true or false
	 * 
	 * 
	 */


	public boolean updateEmail(String[] msg) {
		Statement stm;
		try {
			PreparedStatement ps = conn.prepareStatement("UPDATE project.visitors SET email=? WHERE ID=?");
			ps.setString(1, msg[1]);
			ps.setString(2, msg[0]);
			ps.executeUpdate();
			return true;
		}catch (SQLException e) {e.printStackTrace();
									return false;}
		
		
		
	}
	public boolean canGetEmployee(String userName) { //method checks if employee exists on our DB (By UserName)
		Statement stm;
		try {
			PreparedStatement ps = conn.prepareStatement("SELECT *  FROM project.employees WHERE userName = ?");
			stm = conn.createStatement();
			ps.setString(1, userName);
			ps.executeQuery();
			return true;
		}catch (SQLException e) {e.printStackTrace();
									return false;}
	}
	public String[] getEmployeeUN(String empID) // if employee exists, DB returns this employee as a tuple (String[])
	{
		Statement stm;
		String check[] = empID.split(" ");
		String[] s = new String[12];
		try {
			PreparedStatement ps = conn.prepareStatement("SELECT * FROM project.employees WHERE userName = ?");
			stm = conn.createStatement();
			ps.setString(1, check[0]);
			ResultSet rs = ps.executeQuery();
			s[0] = "EmployeeController"; //For GoClient.HandleMessageFromServer
			s[1] = "IdentifyEmployee"; //in this case we assume that employee is in our DB --> if it isn't it will change
				while(rs.next())
				{
					for (int i=2;i<12;i++) 
						s[i] = rs.getString(i-1);
				}
			if (s[10] == null)
				s[1] = "IdentifyNotExistingEmployee"; //Means that we didn't find an employee with this userName 
			else if (!(s[11].equals(check[1]))) {// It means that passwords do not match and employee can not enter to GoNature system
				s[1] = "IdentifyPasswordDoesNotMatch";
				s[11] = null;
			}
		}catch(SQLException e) {e.printStackTrace();}
		return s;
	}
	public boolean canGetTraveller(String travellerID) //Checks if traveller exists in our DB (By ID)
	{
		Statement stm;
		try {
			PreparedStatement ps = conn.prepareStatement("SELECT *  FROM project.traveller WHERE ID = ? OR MemberID = ?");
			ps.setString(1, travellerID);
			ps.setString(2,  travellerID);
			ps.executeQuery();
			return true;
		}catch (SQLException e) {e.printStackTrace();
									return false;
		}
	}
	public String[] getTravellerFromDB(String travellerID) //returns a String[] with this traveller info 
	{
		int flag = 0;
		Statement stm;
		String[] s = new String[7]; //should be as number fields number in traveller class
		//I am working currently on a DB with 5 fields for traveller. It works
		try {
			PreparedStatement ps = conn.prepareStatement("SELECT * FROM project.traveller WHERE ID=? OR MemberID = ?");
			stm = conn.createStatement();
			ps.setString(1, travellerID);
			ps.setString(2, travellerID);
			ResultSet rs = ps.executeQuery();
			s[0] = "UserController"; //for use of GoClient.HandleMessageFromServer
			s[1] = "IdentifyTraveller";
			while(rs.next())
				for (int i=2;i<7;i++)
					s[i] = rs.getString(i-1);
					//System.out.print(rs.getString(i).toString());
			if (s[2] == null)
			{
				s[1] = "IdentifyNotExistingTraveller";
			}
		}catch(SQLException e) {e.printStackTrace();}
		return s;
	}
	
}
