package SqlConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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
			PreparedStatement ps = conn.prepareStatement("SELECT * FROM project.peson WHERE ID=?");
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
					"INSERT project.peson SET ID=?, firstName=?, lastName=?, phoneNumber=?, Email=?, creditCardNum=? ,maxFamilyMembers=? ,memberId=?");
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
		PreparedStatement ps = conn.prepareStatement("SELECT * FROM project.peson");
		stm = conn.createStatement();
		ResultSet rs = ps.executeQuery();

		while (rs.next()) {
			i++;
		}

	} catch (SQLException e) {
		e.printStackTrace();
	}
	i= i/9;
	return i++;
}
}
