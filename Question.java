package project_package;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class Question {
	public static Scanner scn = new Scanner(System.in);
	
	public static void processAddQuestion(int pID, Connection conn) throws SQLException {
		System.out.println("Enter question string: ");
		String questionString = scn.nextLine();
		System.out.println("Choose type of question: ");
		System.out.println("1 for multiple choice question ");
		System.out.println("2 for open question");
		int type = scn.nextInt();
		
		while (type != 1 && type != 2) {		// Assuring validation
			System.out.println("Invalid input. Please try again: ");
			type = scn.nextInt();
		}
		System.out.println("Enter difficulty: Easy/Intermediate/Hard");
		String diff = scn.next();
		while (!diff.equals("Easy") && !diff.equals("Intermediate") && !diff.equals("Hard")) {	// Assuring input is valid
			System.out.println("Invalid input. Please try again (use the correct format): ");
			diff = scn.next();
		}
		
		int qID = addQuestionToDB(pID, conn, questionString, type, diff);
		
		if (type == 1) {		// Case of a multiple choice question
			System.out.println("Enter the amount of answers: ");
			int numOfAnswers = scn.nextInt();
			scn.nextLine();
			
			while(numOfAnswers < 1) {
				System.out.println("Number of answers should be positive. Try again: ");
				numOfAnswers = scn.nextInt();
			}
			
			for (int i = 0; i < numOfAnswers; i++) {
				System.out.println("Enter answer: ");
				String answerString = scn.nextLine();
				System.out.println("Is it a correct answer?\nPlease enter yes or no: ");
				String isRight = scn.next();
				scn.nextLine();													// Clearing the input stream
				
				while (!isRight.equals("yes") && !isRight.equals("no")) {		// Assuring the input is valid
					System.out.println("Invalid input, please try again: ");
					isRight = scn.nextLine();
				}
				boolean isCorrect;
				if (isRight.equals("yes"))
					isCorrect = true;
				else
					isCorrect = false;
				
				Answer.addAnswerToDB(qID, conn, answerString, isCorrect);
				
			}
		} else {
			// Case of an open question
			System.out.println("Enter answer: ");
			// Clearing the input stream
			scn.nextLine();
			String answerString = scn.nextLine();
			Answer.addAnswerToDB(qID, conn, answerString, true);
		}
	}
	
	public static int addQuestionToDB(int pID, Connection conn, String qString, int type, String difficulty) throws SQLException {
	    // Use RETURNING QID to get the auto-generated QID
	    String newQuestion = "INSERT INTO question(PID, qstring, qtype, difficulty) VALUES(?, ?, ?, ?) RETURNING QID";

	    try (PreparedStatement pstmt = conn.prepareStatement(newQuestion)) {
	        pstmt.setInt(1, pID); 						// Set the profession ID (PID)
	        pstmt.setString(2, qString); 				// Set the question string (qString)
	        pstmt.setInt(3, type); 						// Set the question type (qType)
	        pstmt.setString(4, difficulty.toString()); 	// Set the difficulty level (Difficulty)

	        ResultSet rs = pstmt.executeQuery();		// Execute the insert and retrieve the generated QID
	        if (rs.next()) {
	            int qID = rs.getInt("QID"); 			// Get the generated QID
	            System.out.println("New question inserted successfully!");
	            return qID; 							// Return the new question ID
	        } else {
	            System.out.println("Failed to retrieve QID.");
	            return -1; 								// Indicate failure to retrieve QID
	        }

	    } catch (SQLException ex) {
	        System.out.println("SQL Error: " + ex.getMessage());
	        return -1; 									// Indicate failure due to SQL error
	    }
	}
	
	public static void updateQuestion(int pID, Connection conn) throws SQLException {
		General.printDB(pID, conn);
		System.out.println("Enter the question's ID you want to update:");
		int qID = scn.nextInt();
		qID = General.validateQuestionID(conn, qID, pID, "Enter the question's ID you want to update:");
		scn.nextLine();
		System.out.println("Enter your new Question: ");
		String newQuestion = scn.nextLine();
		String updateQuestion = "UPDATE question SET qstring = ? WHERE qid = ?";

	    try (PreparedStatement pstmt = conn.prepareStatement(updateQuestion)) {
	        // Set the new question string and the question ID
	        pstmt.setString(1, newQuestion);
	        pstmt.setInt(2, qID);

	        // Execute the update
	        int rowsAffected = pstmt.executeUpdate();
	        if (rowsAffected > 0) {
	            System.out.println("Question string updated successfully.\n");
	        } else {
	            System.out.println("Failed to update the question string. Question ID may not exist.\n");
	        }
	    } catch (SQLException ex) {
	        System.out.println("SQL Error: " + ex.getMessage());
	        System.out.println();
	    }
	}
	
	public static void showQuestions(ResultSet rs) throws SQLException {
	    if (rs.isBeforeFirst()) { 		// Check if there are any results
	        System.out.println("The questions are: ");
	        while (rs.next()) {
	            System.out.println("|ID: " + rs.getInt("QID") + "| " + rs.getString("qString"));
	        }
	        System.out.println();
	    } else {						
	        System.out.println("There are no questions available.");
	    }   
	}
	
	public static int getNumOfQuestions(Connection conn, int pID) throws SQLException {

        String countQuestionsSQL = "SELECT COUNT(*) AS totalQuestions FROM Question WHERE PID = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(countQuestionsSQL)) {
            pstmt.setInt(1, pID);							// Set the profession ID
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("totalQuestions"); 		// Return the total number of questions
            } else {
                System.out.println("Failed to count questions.");
                return 0;
            }

        } catch (SQLException ex) {
            System.out.println("SQL Error: " + ex.getMessage());
            return 0;
        }
    }
	
	public static void deleteQuestionFromDB(int pID, Connection conn) throws SQLException {
	 	System.out.println("Enter the question's ID you want to delete: ");
		int qID = scn.nextInt();
		qID = General.validateQuestionID(conn, qID, pID, "Enter the question's ID you want to delete: ");
        // SQL query to delete the question
        String deleteQuestion = "DELETE FROM question WHERE qid = ?";

        // Delete the question itself
        try (PreparedStatement pstmt = conn.prepareStatement(deleteQuestion)) {
            pstmt.setInt(1, qID);

            int numAffectedRows = pstmt.executeUpdate();
            if (numAffectedRows > 0) {
                System.out.println("Question with ID " + qID + " deleted successfully.");
            } else {
                System.out.println("No question found with ID " + qID);
            }
        } catch (SQLException ex) {
            System.out.println("SQL Error while deleting question: " + ex.getMessage());
        }
        System.out.println();
    }

}
