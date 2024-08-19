package project_package;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class Profession {
	public static Scanner scn = new Scanner(System.in);
	
	public static int chooseProfessionDatabase(Connection conn, String msg) throws SQLException {
        Statement stmt = conn.createStatement();
        showProfessions(stmt);
        System.out.println(msg);
        int pID = scn.nextInt();
        scn.nextLine(); 

        // Validate the entered ID
        pID = General.validateProfessionID(conn, pID, msg);

        stmt.close(); // Close the Statement

        return pID;
    }
	
	public static void showProfessions(Statement stmt) throws SQLException {
        ResultSet rs = stmt.executeQuery("SELECT * FROM Profession"); 	// Execute the SQL query to retrieve all professions
        if(rs.isBeforeFirst())			// Check if the result set contains any data
        {
        	System.out.println("The professions are: ");
            while (rs.next()) {			// Iterate through the result set and print each profession's name and ID
                System.out.println("|ID: " + rs.getInt("PID") + "| " + rs.getString("pName"));
            }
        }
        else
            System.out.println("There are no professions.");


        System.out.println();
    }
	
	public static void addProfession(Connection conn) throws SQLException {
        System.out.println("Enter profession's title: ");
        String title = scn.nextLine();

        String newProfession = "INSERT INTO profession (pName) VALUES(?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(newProfession)) {
            pstmt.setString(1, title); // Sets the profession's title in the query
            
            int numAffectedRows = pstmt.executeUpdate();
            if (numAffectedRows > 0) {
                System.out.println("New profession inserted successfully: " + title);
            } else {
                System.out.println("Failed to insert the profession.");
            }
            System.out.println();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }
	
	public static void deleteProfessionByIDFromDB(Connection conn) throws SQLException {
        String msg = "Enter the profession's ID you want to delete: ";
        int pid = chooseProfessionDatabase(conn, msg);
        String deleteProfession = "DELETE FROM profession WHERE pid = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(deleteProfession)) {
            pstmt.setInt(1, pid);

            int numAffectedRows = pstmt.executeUpdate();
            if (numAffectedRows > 0) {
                System.out.println("Profession with ID " + pid + " deleted successfully.");
            } else {
                System.out.println("No profession found with ID " + pid);
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        System.out.println();
    }


}
