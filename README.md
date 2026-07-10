Library Management System (Java)

A console-based Library Management System built in core Java, demonstrating Object-Oriented Programming, the Java Collections Framework, exception handling, and file-based data persistence.

Features


Book Management — add, remove, and view books in the catalog
Member Management — register library members
Issue / Return — issue books to members and process returns
Search — find books by title or author
Fine Calculation — automatically calculates overdue fines (₹5/day after a 14-day loan period)
Data Persistence — book and member records are saved to text files (books.txt, members.txt) and reloaded on the next run


Concepts Demonstrated

ConceptWhere it's usedEncapsulationPrivate fields with getters/setters in Book and MemberInheritancePerson (abstract) → MemberPolymorphismOverridden getRole() methodCollections FrameworkHashMap for catalog/member lookup, ArrayList for search resultsException HandlingCustom checked exceptions (BookNotFoundException, BookNotAvailableException, MemberNotFoundException, DuplicateIdException)File HandlingBufferedReader / BufferedWriter for reading and writing records

How to Run

Requirements: JDK 8 or later

bash# Compile
javac LibraryManagementSystem.java

# Run
java LibraryManagementSystem

Menu Options

1. Add Book
2. Remove Book
3. Register Member
4. Issue Book
5. Return Book
6. Search Books
7. Display All Books
8. Display All Members
9. Save & Exit

Project Structure

LibraryManagementSystem.java   # All classes: Book, Person, Member, Library, custom exceptions, main app
books.txt                      # Auto-generated — stores book records
members.txt                    # Auto-generated — stores member records

Sample Usage

Enter your choice: 1
Book ID: B001
Title: The Alchemist
Author: Paulo Coelho
Book added successfully.

Enter your choice: 3
Member ID: M001
Name: Vanshika
Member registered successfully.

Enter your choice: 4
Book ID: B001
Member ID: M001
Book "The Alchemist" issued to Vanshika. Due date: 2026-07-24

Author

Vanshika
