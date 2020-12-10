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
	
	
}
