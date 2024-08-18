package project_package;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.InputMismatchException;
import java.util.Scanner;

public class General {
	public static Scanner scn = new Scanner(System.in);

	public static void printDB(int pID, Connection conn) throws SQLException {
	    String query = "SELECT * FROM Question WHERE pid = ?";
	    try (PreparedStatement pstmt = conn.prepareStatement(query)) {
	        pstmt.setInt(1, pID);										// Set the profession ID
	        try (ResultSet rs = pstmt.executeQuery()) {
	            Question.showQuestions(rs);
	        }
	    }
	}
	
	public static int validateProfessionID(Connection conn, int id, String msg) throws SQLException {
		boolean isValid = false;

		while (!isValid) {
			try {
				String query = "SELECT COUNT(*) FROM Profession WHERE PID = ?";
				try (PreparedStatement pstmt = conn.prepareStatement(query)) {
					pstmt.setInt(1, id);
					try (ResultSet rs = pstmt.executeQuery()) {
						if (rs.next() && rs.getInt(1) > 0) {
							isValid = true; // The ID is valid
						} else {
							System.out.println("No such Profession ID. \n");
							Profession.showProfessions(conn.createStatement());
							System.out.println(msg);
							id = promptForNewID();
						}
					}
				}
			} catch (InputMismatchException e) {
				handleInputMismatch(msg);
			}
		}
		return id;
	}

	public static int validateQuestionID(Connection conn, int id, int pID, String msg) throws SQLException {
	    boolean isValid = false;

	    while (!isValid) {
	        try {
	            String query = "SELECT COUNT(*) FROM Question WHERE QID = ? AND PID = ?";
	            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
	                pstmt.setInt(1, id);
	                pstmt.setInt(2, pID); // Ensure that QID also matches the correct PID
	                try (ResultSet rs = pstmt.executeQuery()) {
	                    if (rs.next() && rs.getInt(1) > 0) {
	                        isValid = true; // The ID is valid
	                    } else {
	                        System.out.println("No such Question ID for the given Profession ID. \n");
	                        printDB(pID, conn); // Use the correct PID to show related questions
	                        System.out.println(msg);
	                        id = promptForNewID();
	                    }
	                }
	            }
	        } catch (InputMismatchException e) {
	            handleInputMismatch(msg);
	        }
	    }
	    return id;
	}


	public static int validateAnswerID(Connection conn, int id, int qID, String msg) throws SQLException {
	    boolean isValid = false;

	    while (!isValid) {
	        try {
	            String query = "SELECT COUNT(*) FROM Answer WHERE AID = ? AND QID = ?";
	            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
	                pstmt.setInt(1, id);
	                pstmt.setInt(2, qID); // Ensure that AID also matches the correct QID
	                try (ResultSet rs = pstmt.executeQuery()) {
	                    if (rs.next() && rs.getInt(1) > 0) {
	                        isValid = true; // The ID is valid
	                    } else {
	                        System.out.println("No such Answer ID. \n");
	                        Answer.showAnswers(qID, conn); // Show answers related to the given QID
	                        System.out.println(msg);
	                        id = promptForNewID();
	                    }
	                }
	            }
	        } catch (InputMismatchException e) {
	            handleInputMismatch(msg);
	        }
	    }
	    return id;
	}

	private static int promptForNewID() {
		int newID = -1;
		if (scn.hasNextInt()) {
			newID = scn.nextInt();
			scn.nextLine(); // Consume any leftover newline character
		} else {
			System.out.println("Invalid input. Please enter a numeric ID.");
			scn.next(); // Consume the invalid input to avoid an infinite loop
		}
		return newID;
	}

	private static void handleInputMismatch(String msg) {
		System.out.println("Invalid input. Please enter a numeric ID.");
		scn.next(); // Consume the invalid input to avoid an infinite loop
		System.out.println(msg);
	}

}
	
	


