package org.hibernate.testing.cleaner;

import java.sql.ResultSet;
import java.sql.Statement;

import org.junit.jupiter.api.Test;

public class JdbcConnectionContextTests {

	@Test
	public void testConnection() {
		JdbcConnectionContext.work( conn -> {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SHOW SCHEMAS");
			
			while (rs.next())
				System.out.println(rs.getString(1));
		});
	}
}
