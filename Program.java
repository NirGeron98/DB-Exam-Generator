package project_package;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Scanner;

public class Program {
	public static Scanner scn = new Scanner(System.in);
	final static String PASSWORD = "password";	

	public static void databaseMenu(int pID, Connection conn, int uID)
			throws FileNotFoundException, SQLException {
		final int SHOW_DATABASE = 1, SHOW_ANSWERS = 2, ADD_QUESTION = 3, ADD_ANSWER = 4, DELETE_ANSWER = 5, DELETE_QUESTION = 6,
				UPDATE_QUESTION = 7, UPDATE_ANSWER = 8, GENERATE_EXAM = 9, SHOW_EXAMS = 10, EXIT = 0;
		int choice;
		do {

			System.out.println("\nPlease choose an option from the following:");
			System.out.println("1. Show all the questions in the database.");
			System.out.println("2. Show all the answers of a question.");
			System.out.println("3. Add a new question to the database.");
			System.out.println("4. Add a new answer to an existing question.");
			System.out.println("5. Delete an answer from a question.");
			System.out.println("6. Delete a question from database.");
			System.out.println("7. Update a question.");
			System.out.println("8. Update an answer.");
			System.out.println("9. Generate a new exam.");
			System.out.println("10.Show ID of the exams you created");
			System.out.println("0. Return to the previous menu.");
			choice = scn.nextInt();
			// Clearing the input stream.
			scn.nextLine();
			switch (choice) {
			case SHOW_DATABASE:					// 1
				General.printDB(pID, conn);
				break;
			case SHOW_ANSWERS:					// 2
				Answer.showAnswersOfQuestion(pID, conn);
				break;
			case ADD_QUESTION:					// 3
				Question.processAddQuestion(pID, conn);	
				break;
			case ADD_ANSWER:					// 4
				Answer.processAddAnswer(pID, conn);	
				break;
			case DELETE_ANSWER:					// 5
				Answer.processDeleteAnswer(pID, conn);
				break;
			case DELETE_QUESTION:				// 6
				General.printDB(pID, conn);
				Question.deleteQuestionFromDB(pID, conn);
				break;
			case UPDATE_QUESTION:				// 7
				Question.updateQuestion(pID, conn);
				break;
			case UPDATE_ANSWER:					// 8
				Answer.updateAnswer(pID, conn);
				break;
			case GENERATE_EXAM:					// 9
				Exam.generateExam(pID, uID, conn);
				break;
			case SHOW_EXAMS:					// 10
				Exam.showExamsByUID(pID, uID, conn);
				break;
			case EXIT:							// 0
				System.out.println("Returning to previous menu... \n");
				break;
			default:
				System.out.println("Invalid input, please try again: ");
				break;
			}

		} while (choice != 0);
	}
	

	public static boolean startMenu(Connection conn, int uID)
	        throws FileNotFoundException, SQLException {
	    Statement stmt = conn.createStatement();
	    final int SHOW_DATABASE = 1, CHOOSE_DATABASE = 2, ADD_PROFESSION = 3, DELETE_DATABASE = 4, LOGOUT = 5, EXIT = 0;
	    int choice;
	    do {
	        System.out.println("Choose one of the following: ");
	        System.out.println("1. Show all professions.");
	        System.out.println("2. Choose an existing profession.");
	        System.out.println("3. Add a new profession.");
	        System.out.println("4. Delete an existing profession.");
	        System.out.println("5. Logout.");
	        System.out.println("0. Exit the program.");
	        choice = scn.nextInt();
	        scn.nextLine(); // Clear the input stream
	        switch (choice) {
	            case SHOW_DATABASE:				// 1
	            	Profession.showProfessions(stmt);
	                break;
	            case CHOOSE_DATABASE:			// 2
	                int pid = Profession.chooseProfessionDatabase(conn, "Choose the profession's ID:");
	                databaseMenu(pid, conn, uID);
	                break;
	            case ADD_PROFESSION:			// 3
	            	Profession.addProfession(conn);
	                break;
	            case DELETE_DATABASE:			// 4
	            	Profession.deleteProfessionByIDFromDB(conn);
	                break;
	            case LOGOUT:					// 5
	                System.out.println("Logging out...\n");
	                return true; 				// Return true to indicate logout
	            case EXIT:						// 0
	                System.out.println("Exiting...");
	                System.out.println("Thanks for using our exam builder Program!");
	                return false; 				// Return false to indicate exit
	            default:
	                System.out.println("Invalid input, please try again: ");
	                break;
	        }
	    } while (choice != 0);

	    return false; 							// Default return for exiting the program
	}
	 
	 
	 public static int getUIDFromUsername(Connection conn, String username) throws SQLException {
		    // SQL query to get the UID based on the username
		    String getUIDSQL = "SELECT UID FROM users WHERE uname = ?";

		    try (PreparedStatement pstmt = conn.prepareStatement(getUIDSQL)) {
		        // Set the username parameter in the query
		        pstmt.setString(1, username);

		        // Execute the query and get the result set
		        ResultSet rs = pstmt.executeQuery();

		        if (rs.next()) {
		            return rs.getInt("UID"); // Return the UID
		        } else {
		            System.out.println("No user found with the username: " + username);
		            return -1; // Indicate that no user was found
		        }

		    } catch (SQLException ex) {
		        System.out.println("SQL Error: " + ex.getMessage());
		        return -1; // Indicate failure due to SQL error
		    }
		}
	 

	    public static void main(String[] args)
	            throws ClassNotFoundException, IOException, SQLException {

	        // Establish database connection
	        Connection conn = connectToDatabase("jdbc:postgresql://localhost:5432/Exam_Project", "postgres", PASSWORD);
	        if (conn == null) {
	            System.out.println("Failed to establish connection to the database. Exiting...");
	            return;
	        }

	        // Main loop to handle login and menu navigation
	        boolean exitProgram = false;
	        while (!exitProgram) {
	            exitProgram = handleLoginAndMenu(conn);			// Display Login GUI - if success go to menu, else exit program
	        }

	        scn.close();										// Close scanner
	        conn.close();										// Close connection
	    }

	    private static Connection connectToDatabase(String dbUrl, String username, String password) throws ClassNotFoundException {
	        Connection conn = null;
	        Class.forName("org.postgresql.Driver");
	        try {
	            conn = DriverManager.getConnection(dbUrl, username, password);
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	        return conn;
	    }

	    private static boolean handleLoginAndMenu(Connection conn) throws SQLException, IOException {
	        Map<String, Object> res = LoginGUI.createAndShowGUI(conn);			// Return username, and boolean (if the login success)
	        boolean loginSuccess = (boolean) res.get("loginSuccess");	
	        if (loginSuccess) {											// If login success
	            String username = (String) res.get("username");			// Save username string 
	            System.out.println("Welcome, " + username + "!");		// Welcome message
	            int uID = getUIDFromUsername(conn, username);			// Get uID from username
	            return !startMenu(conn, uID); 							// Return true to exit if the user chooses to logout
	        } else {
	            System.out.println("Login failed. Exiting...");
	            return true; 											// Return true to exit the program on login failure
	        }
	    }


}
