package project_package;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class Exam {
	public static Scanner scn = new Scanner(System.in);

	public static void generateExam(int pID, int uID,  Connection conn) throws SQLException {
        System.out.println("Choose exam generation method: 1 for Automatic, 2 for Manual");
        int choice = scn.nextInt();
        while(choice != 1 && choice != 2) {
        	System.out.println("Invalid input, choose exam generation method: 1 for Automatic, 2 for Manual");
            choice = scn.nextInt();
        }
        
        String query = "SELECT uName FROM Users WHERE uID = ?";
        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setInt(1, uID);							// Set the user ID
        ResultSet rs = pstmt.executeQuery();
        rs.next();
        String uName = rs.getString("uName");			// get username from user ID
        
        if (choice == 1) {								// Automatic Exam
            generateAutomaticExam(pID, uID, uName, conn);
        }
        else if (choice == 2) {							// Manual Exam
            generateManualExam(pID, uID, uName, conn);
        }
        else 
            System.out.println("Invalid choice. Please select 1 for Automatic or 2 for Manual.");    
    }

    private static void generateAutomaticExam(int pID, int uID, String uName, Connection conn) {
        try {
            int totalQuestions = Question.getNumOfQuestions(conn, pID);
            System.out.println("Total available questions for this profession: " + totalQuestions);

            if (totalQuestions == 0) {
                System.out.println("There are no questions in this profession yet.");
                return;
            }

            System.out.println("How many questions do you want in the exam?");
            int selectedQuestions = scn.nextInt();

            while (selectedQuestions <= 0 || selectedQuestions > totalQuestions) {
                System.out.println("Invalid number. Please enter a number between 1 and " + totalQuestions + ":");
                selectedQuestions = scn.nextInt();
            }

            // Fetch selected number of random questions with the given PID from the Question table
            String questionQuery = "SELECT QID FROM Question WHERE PID = ? ORDER BY RANDOM() LIMIT ?";
            PreparedStatement questionStmt = conn.prepareStatement(questionQuery);
            questionStmt.setInt(1, pID);				// Set the profession ID
            questionStmt.setInt(2, selectedQuestions);	
            ResultSet questionRs = questionStmt.executeQuery();

            // Insert an entry into the Exam table
            String examInsertQuery = "INSERT INTO Exam (UID, PID) VALUES (?, ?)";
            PreparedStatement examStmt = conn.prepareStatement(examInsertQuery, Statement.RETURN_GENERATED_KEYS);
            examStmt.setInt(1, uID);			// Set the user ID
            examStmt.setInt(2, pID);			// Set the profession ID
            examStmt.executeUpdate();

            // Retrieve the generated EID
            ResultSet examRs = examStmt.getGeneratedKeys();
            int eID = 0;
            if (examRs.next()) {
                eID = examRs.getInt(1);
            }

            // Insert selected questions into the ExamQuestion table
            while (questionRs.next()) {
                int qID = questionRs.getInt("QID");

                String examQuestionInsert = "INSERT INTO ExamQuestion (EID, QID) VALUES (?, ?)";
                PreparedStatement examQuestionStmt = conn.prepareStatement(examQuestionInsert);
                examQuestionStmt.setInt(1, eID);		// Set the exam ID
                examQuestionStmt.setInt(2, qID);		// set the question ID
                examQuestionStmt.executeUpdate();
            }

            System.out.println("Automatic exam generation complete.");
            generateExamFiles(eID, pID, uName, conn);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void generateManualExam(int pID, int uID, String uName, Connection conn) {
        try {
            int numQuestionsAvailable = Question.getNumOfQuestions(conn, pID);
            if (numQuestionsAvailable == 0) {
                System.out.println("There are no questions available for this profession.");
                return;
            }
         
            System.out.println("Available questions:");
            String questionQuery = "SELECT QID, qString FROM Question WHERE PID = ?";
            PreparedStatement questionStmt = conn.prepareStatement(questionQuery);
            questionStmt.setInt(1, pID);				// Set the profession ID
            ResultSet questionRs = questionStmt.executeQuery();

            while (questionRs.next()) {
                int qID = questionRs.getInt("QID");		// get question ID
                String questionText = questionRs.getString("qString");
                System.out.println("Question ID: " + qID + " - " + questionText);
            }

            System.out.println("How many questions would you like to include in the exam? (1-"+numQuestionsAvailable+")");
            int numQuestions = scn.nextInt();
            while (numQuestions <= 0 || numQuestions > numQuestionsAvailable) {			// Assuring the input is valid
                System.out.println("Invalid number. Please choose a number between 1 and " + numQuestionsAvailable + ":");
                numQuestions = scn.nextInt();
            }

            String examInsertQuery = "INSERT INTO Exam (UID, PID) VALUES (?, ?)";
            PreparedStatement examStmt = conn.prepareStatement(examInsertQuery, Statement.RETURN_GENERATED_KEYS);
            examStmt.setInt(1, uID);
            examStmt.setInt(2, pID);
            examStmt.executeUpdate();

            ResultSet examRs = examStmt.getGeneratedKeys();
            int eID = 0;
            if (examRs.next()) {
                eID = examRs.getInt(1);
                System.out.println("New exam created!");
            }

            for (int i = 0; i < numQuestions; i++) {
                int qID = -1;
                boolean validQID = false;

                while (!validQID) {
                    System.out.println("Enter the question's ID you want to add to the exam:");
                    qID = scn.nextInt();

                    // Check if the question already exists in the exam
                    String checkQuery = """
                        SELECT COUNT(*) 
                        FROM ExamQuestion 
                        WHERE EID = ? AND QID = ?
                        """;
                    PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
                    checkStmt.setInt(1, eID);						// Set exam ID
                    checkStmt.setInt(2, qID);						// Set question id
                    ResultSet checkRs = checkStmt.executeQuery();

                    if (checkRs.next() && checkRs.getInt(1) > 0) {	// Check if the question is already been added to the exam
                        System.out.println("This question has already been added. Please select a different question ID.");
                        continue;
                    }

                    String verifyQIDQuery = "SELECT COUNT(*) FROM Question WHERE QID = ? AND PID = ?";
                    PreparedStatement verifyStmt = conn.prepareStatement(verifyQIDQuery);
                    verifyStmt.setInt(1, qID);							// set question ID
                    verifyStmt.setInt(2, pID);							// set profession ID
                    ResultSet verifyRs = verifyStmt.executeQuery();

                    if (verifyRs.next() && verifyRs.getInt(1) > 0) {	// Assuring validation
                        validQID = true;

                        String examQuestionInsert = "INSERT INTO ExamQuestion (EID, QID) VALUES (?, ?)";
                        PreparedStatement examQuestionStmt = conn.prepareStatement(examQuestionInsert);
                        examQuestionStmt.setInt(1, eID);				// set exam ID
                        examQuestionStmt.setInt(2, qID);				// set question ID
                        examQuestionStmt.executeUpdate();

                        System.out.println("The question has been added to the exam");
                    } else {	
                        System.out.println("Invalid ID. Please enter a valid Question ID from the list above.");
                    }
                }
            }

            System.out.println("Manual exam creation complete.");
            generateExamFiles(eID, pID, uName, conn);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private static void generateExamFiles(int eID, int pID, String uName, Connection conn) {
        try {
            File solutionFile = new File("Solution_for_exam_"+eID +"_created by_" +uName + ".txt");			// Solution name
            PrintWriter solutionPw = new PrintWriter(solutionFile);											// Write the solution file name 

            File examFile = new File("Exam_" + eID +"_created_by_"+ uName+ ".txt");							// Test name
            PrintWriter examPw = new PrintWriter(examFile);													// Write the text file name 

            String query = """
                SELECT q.QID, q.qString, q.qType, a.aString, a.isCorrect 
                FROM ExamQuestion eq
                JOIN Question q ON eq.QID = q.QID
                LEFT JOIN Answer a ON q.QID = a.QID
                WHERE eq.EID = ?
                ORDER BY q.QID
                """;
            
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, eID);			// Set exam ID
            ResultSet rs = stmt.executeQuery();

            int questionNumber = 1;
            String lastQuestion = null;
            int currentQuestionType = -1;
           

            while (rs.next()) {
                String questionText = rs.getString("qString");			// Get question
                int questionType = rs.getInt("qType");					// Get question's type
                String answerText =rs.getString("aString");				// get answer
                boolean isCorrect = rs.getBoolean("isCorrect");			// Get true/false (if the answer is correct)
                if (!questionText.equals(lastQuestion)) {
                    if (questionNumber > 1) {
                        solutionPw.println();
                        examPw.println();
                    }
                    solutionPw.println("Question  " + questionNumber + ":  " + questionText);		// Write Question to solution file 
                    examPw.println("Question  " + questionNumber + ":  " +questionText ); 			// Write Question to exam file
                    questionNumber++;
                    currentQuestionType = questionType; 					// Update the question type for the current question
                }
           
                if (currentQuestionType == 1) { 							// Multiple answers (type 1)
                    if (answerText != null) {
                        examPw.println("- " + answerText);	
                        if (isCorrect) {
                            solutionPw.println("Answer: " + answerText);	// Write the correct answer
                        }
                     
                    }
                } else if (currentQuestionType == 2) { 						// Single answer (type 2)
                    if (answerText != null && isCorrect) {
                        solutionPw.println("Answer: " + answerText);		// Write the correct answer
                        examPw.println("______________________________________");
                    }                
                }

                lastQuestion = questionText;
            }

            solutionPw.close();				
            examPw.close();

            System.out.println("Exam files generated successfully!\n");
        } catch (SQLException | FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    
    public static void showExamsByUID(int pID, int uID, Connection conn) throws SQLException {
        // SQL query to retrieve all ExamIDs along with the user's name and profession's name
        String query = "SELECT e.eid, u.uname, p.pname FROM exam e " +
                       "JOIN users u ON e.uid = u.uid " +
                       "JOIN profession p ON e.pid = p.pid " +
                       "WHERE e.uid = ? AND e.pid = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            // Set the UID and PID parameters
            pstmt.setInt(1, uID);
            pstmt.setInt(2, pID);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.isBeforeFirst()) { 	// Check if there are any results
                    rs.next(); 				// Move to the first row to access the user's name and profession's name
                    String userName = rs.getString("uname");
                    String professionName = rs.getString("pname");
                    System.out.println("The exams created by user " + userName + " for profession " + professionName + " are:");
                    System.out.println("Exam ID: " + rs.getInt("eid"));

                    // Print remaining exam IDs
                    while (rs.next()) {
                        System.out.println("Exam ID: " + rs.getInt("eid"));
                    }
                } else {
                    System.out.println("No exams found for this user.");
                }
            }
        } catch (SQLException ex) {
            System.out.println("SQL Error: " + ex.getMessage());
        }
        System.out.println();
    }


}
