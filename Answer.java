package project_package;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class Answer {
	public static Scanner scn = new Scanner(System.in);
	
	public static void addAnswerToDB(int qID, Connection conn, String aString, boolean isCorrect) throws SQLException {
        String SQL = "INSERT INTO answer(qid, astring, isCorrect) VALUES(?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(SQL)) {

            // Set the values for the placeholders
            pstmt.setInt(1, qID); 				// Set the question ID
            pstmt.setString(2, aString); 		// Set the answer string
            pstmt.setBoolean(3, isCorrect); 	// Set whether the answer is correct

            // Execute the insert statement
            int numAffectedRows = pstmt.executeUpdate();
            if (numAffectedRows > 0) {
                System.out.println("New answer inserted successfully.\n");
            } else {
                System.out.println("Failed to insert the answer.\n");
            }

        } catch (SQLException ex) {
            System.out.println("SQL Error: " + ex.getMessage());
        }
    }
	
	public static void deleteAnswerFromDB(int aID, Connection conn) {
	    String deleteAnswer = "DELETE FROM answer WHERE aid = ?";

	    try (PreparedStatement pstmt = conn.prepareStatement(deleteAnswer)) {
	        // Set the value for the placeholder
	        pstmt.setInt(1, aID);

	        // Execute the delete statement
	        int numAffectedRows = pstmt.executeUpdate();
	        if (numAffectedRows > 0) {
	            System.out.println("Answer with ID " + aID + " deleted successfully.");
	        } else {
	            System.out.println("No answer found with ID " + aID);
	        }
	    } catch (SQLException ex) {
	        System.out.println(ex.getMessage());
	    }
	    System.out.println();
	}
	
	public static void updateAnswer(int pID, Connection conn) throws SQLException {
	    // Display all answers
		General.printDB(pID, conn);
	    System.out.println("Enter the question's ID to display its answers:");
	    int qID = scn.nextInt();
	    scn.nextLine(); 								// Clear buffer
	    qID = General.validateQuestionID(conn, qID, pID, "Enter the question's ID to display its answers:");
	    showAnswers(qID, conn);							// Print question's answers
	    
	    System.out.println("Enter the Answer ID you want to update:");
	    int aID = scn.nextInt();
	    aID = General.validateAnswerID(conn, aID, qID, "Enter the Answer ID you want to update:");	 	
	    scn.nextLine(); 
	    
	    System.out.println("Enter your new answer text:");
	    String newAnswer = scn.nextLine();

	    // SQL query to update the answer string
	    String updateAnswer = "UPDATE answer SET astring = ? WHERE aid = ?";

	    try (PreparedStatement pstmt = conn.prepareStatement(updateAnswer)) {
	        pstmt.setString(1, newAnswer);				// Set the new answer string
	        pstmt.setInt(2, aID);						// Set the answer ID
	      
	        // Execute the update
	        int rowsAffected = pstmt.executeUpdate();
	        if (rowsAffected > 0) {
	            System.out.println("Answer updated successfully.");
	        } else {
	            System.out.println("Failed to update the answer. Answer ID may not exist.\n");
	        }
	    } catch (SQLException ex) {
	        System.out.println("SQL Error: " + ex.getMessage());
	        System.out.println();
	    }
	}
	
	public static void showAnswers(int qID, Connection conn) throws SQLException {
		String answer = "SELECT aid, astring, isCorrect FROM answer WHERE qid = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(answer)) {
            // Set the value for the placeholder
            pstmt.setInt(1, qID);

            // Execute the query and get the result set
            ResultSet rs = pstmt.executeQuery();

            // Check if any answers were found
            boolean hasAnswers = false;
            while (rs.next()) {
                hasAnswers = true;
                int aID = rs.getInt("aid");
                String aString = rs.getString("astring");
                boolean isCorrect = rs.getBoolean("isCorrect");

                // Print the answer details
                showAnswer(aID, aString, isCorrect);
            }
            System.out.println();

            if (!hasAnswers) {
                System.out.println("No answers found for question ID: " + qID);
            }

        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
	}
	
	public static void showAnswer(int aID, String aString, boolean isCorrect) {
		System.out.println("|ID: " + aID + "| "+ aString + " (" + isCorrect + ")");
	}
	
	public static void processAddAnswer(int pID, Connection conn) throws SQLException {
		General.printDB(pID, conn);
	    System.out.println("Enter question's ID: ");
	    int qID = scn.nextInt();
	    qID = General.validateQuestionID(conn, qID, pID, "Enter question's ID: ");

	    // Check the type of the question
	    String query = "SELECT qtype FROM question WHERE qid = ?";
	    try (PreparedStatement pstmt = conn.prepareStatement(query)) {
	        pstmt.setInt(1, qID);		// Set question ID
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            int qType = rs.getInt("qtype");
	            if (qType == 2) { 		// Open question
	                System.out.println("This is an open question. You cannot add another answer.\n");
	                return;
	            }
	        } else {
	            System.out.println("Question ID not found.\n");
	            return;
	        }
	    } catch (SQLException ex) {
	        System.out.println("SQL Error: " + ex.getMessage());
	        System.out.println();
	        return;
	    }
	    
	    scn.nextLine();
	    
	    System.out.println("Enter answer: ");
	    String answerString = scn.nextLine();
	    
	    System.out.println("Is it a correct answer?\nPlease enter yes or no: ");
	    String isRight = scn.next();	
	    scn.nextLine();
	    
	   
	    while (!isRight.equals("yes") && !isRight.equals("no")) {			// Assuring the input is valid
	        System.out.println("Invalid input, please try again: ");
	        isRight = scn.nextLine();
	    }
	    boolean isCorrect = isRight.equals("yes");			// The answer of open questions will always be correct

	    addAnswerToDB(qID, conn, answerString, isCorrect);
	}
	
	public static void processDeleteAnswer(int pID, Connection conn) throws SQLException {
	    General.printDB(pID, conn);
	    System.out.println("Enter the question's ID you want to delete an answer from: ");
	    int qID = scn.nextInt();
	    qID = General.validateQuestionID(conn, qID, pID, "Enter the question's ID you want to delete an answer from: ");
	    showAnswers(qID, conn);
	    System.out.println("Enter the answer's ID you want to delete: ");
	    int aID = scn.nextInt();
	    aID = General.validateAnswerID(conn, aID, qID, "Enter the answer's ID you want to delete: ");

	    // Check if this is the last answer for the question
	    if (isLastAnswer(conn, qID)) {
	        System.out.println("Cannot delete the answer because it is the last answer for this question.\nYou can update the answer.");
	    } else if (isOnlyCorrectAnswer(conn, qID, aID)) {
	        System.out.println("Cannot delete the answer because it is the only correct answer for this question.\nYou can update the answer.");
	    } else if (isBelowTwoAnswers(conn, qID)) {
	        System.out.println("Cannot delete the answer because a closed question must have at least two answers.\n");
	    } else {
	        deleteAnswerFromDB(aID, conn);
	    }
	}

	public static boolean isOnlyCorrectAnswer(Connection conn, int qID, int aID) throws SQLException {
	    String countCorrectAnswersQuery = "SELECT COUNT(*) FROM answer WHERE qid = ? AND isCorrect = TRUE";
	    try (PreparedStatement pstmt = conn.prepareStatement(countCorrectAnswersQuery)) {
	        pstmt.setInt(1, qID);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            int count = rs.getInt(1);
	            if (count == 1) {
	                // Check if the only correct answer is the one being deleted
	                String checkIfDeletingCorrect = "SELECT isCorrect FROM answer WHERE aid = ?";
	                try (PreparedStatement pstmt2 = conn.prepareStatement(checkIfDeletingCorrect)) {
	                    pstmt2.setInt(1, aID);
	                    ResultSet rs2 = pstmt2.executeQuery();
	                    if (rs2.next()) {
	                        return rs2.getBoolean("isCorrect"); // Return true if the answer being deleted is the correct one
	                    }
	                }
	            }
	        }
	    } catch (SQLException ex) {
	        System.out.println("SQL Error: " + ex.getMessage());
	    }
	    return false; // Default to false if there's an issue
	}

	public static boolean isBelowTwoAnswers(Connection conn, int qID) throws SQLException {
	    String countAnswersQuery = "SELECT COUNT(*) FROM answer WHERE qid = ?";
	    try (PreparedStatement pstmt = conn.prepareStatement(countAnswersQuery)) {
	        pstmt.setInt(1, qID);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            int count = rs.getInt(1);
	            return count <= 2; // Return true if there are less than or equal to two answers left
	        }
	    } catch (SQLException ex) {
	        System.out.println("SQL Error: " + ex.getMessage());
	    }
	    return false; // Default to false if there's an issue
	}


	
	public static boolean isLastAnswer(Connection conn, int qID) throws SQLException {
	    String countAnswersQuery = "SELECT COUNT(*) FROM answer WHERE qid = ?";
	    try (PreparedStatement pstmt = conn.prepareStatement(countAnswersQuery)) {
	        pstmt.setInt(1, qID);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            int count = rs.getInt(1);
	            return count <= 1; // Return true if there is only one answer left
	        }
	    } catch (SQLException ex) {
	        System.out.println("SQL Error: " + ex.getMessage());
	    }
	    return false; // Default to false if there's an issue
	}
	
	public static void showAnswersOfQuestion(int pID, Connection conn) throws SQLException {
		General.printDB(pID, conn);
		System.out.println("Enter the ID of the question whose answers you want to view: ");
		int qID = scn.nextInt();
	    qID = General.validateQuestionID(conn, qID, pID, "Enter the ID of the question whose answers you want to view: ");
		showAnswers(qID, conn);
	}


}
