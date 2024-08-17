## About This Project

This project is a continuation of a course project from my Bachelor's degree in Computer Science. Originally developed as part of an Object-Oriented Programming (OOP) course, the initial version utilized arrays and text files for data storage.

As part of a Databases course, I have significantly enhanced the project by replacing the previous storage methods with a robust database solution. The system now uses a PostgreSQL database to manage questions, answers, and user data, providing a more reliable and scalable foundation. While the core functionalities remain aligned with the original project instructions, this updated version leverages the power of relational databases to improve data management and system performance.

### Key Features and Functionalities:

- **Question and Answer Management**: 
  - **Add** new questions and answers to the database with ease.
  - **Update** existing questions and answers to keep the content current.
  - **Delete** questions and their associated answers, or specific answers as needed.
  - **View** all questions and answers, categorized by profession or subject, and see which answers are marked as correct.

- **Profession Management**:
  - **Add** new professions or subjects to the system, allowing for better organization of questions and exams.
  - **Update** existing professions to reflect any changes in curriculum or structure.
  - **Delete** professions that are no longer needed, while maintaining the integrity of the associated data.

- **Test Creation**:
  - **Manual Test Creation**: Select specific questions from the database, customize the answers to include, and generate a test file. This feature ensures that no question is repeated within the same test.
  - **Automated Test Creation**: Automatically generate tests by randomly selecting questions and answers from the database. This ensures a balanced mix of difficulty levels and supports both multiple-choice and open-ended questions.
  - Both methods generate a **solution file** alongside the test file, which includes the correct answers for easy grading and review.

- **Data Integrity and Security**:
  - The transition to a database ensures that all operations, such as adding, updating, and deleting data, are performed with full transactional integrity.
  - **User Authentication**: Users must log in to access the system, with their credentials securely stored and managed in the database.

This transition from text files and arrays to a database-driven architecture marks a key step in my academic journey, bridging concepts from multiple courses and applying them to real-world applications. The system is now more robust, flexible, and scalable, making it an ideal tool for educational institutions to create and manage tests efficiently.
