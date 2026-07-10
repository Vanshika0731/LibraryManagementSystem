import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

/* =========================================================
   CUSTOM EXCEPTIONS  (Exception Handling)
   ========================================================= */
class BookNotFoundException extends Exception {
    public BookNotFoundException(String message) { super(message); }
}

class BookNotAvailableException extends Exception {
    public BookNotAvailableException(String message) { super(message); }
}

class MemberNotFoundException extends Exception {
    public MemberNotFoundException(String message) { super(message); }
}

class DuplicateIdException extends Exception {
    public DuplicateIdException(String message) { super(message); }
}

/* =========================================================
   BOOK CLASS  (Encapsulation)
   ========================================================= */
class Book {
    private String bookId;
    private String title;
    private String author;
    private boolean issued;
    private String issuedTo;      // memberId, "-" if not issued
    private LocalDate issueDate;  // null if not issued

    private static final DateTimeFormatter FMT = DateTimeFormatter.ISO_LOCAL_DATE;

    public Book(String bookId, String title, String author) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.issued = false;
        this.issuedTo = "-";
        this.issueDate = null;
    }

    // Getters / Setters (encapsulation)
    public String getBookId() { return bookId; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public boolean isIssued() { return issued; }
    public String getIssuedTo() { return issuedTo; }
    public LocalDate getIssueDate() { return issueDate; }

    public void markIssued(String memberId) {
        this.issued = true;
        this.issuedTo = memberId;
        this.issueDate = LocalDate.now();
    }

    public void markReturned() {
        this.issued = false;
        this.issuedTo = "-";
        this.issueDate = null;
    }

    @Override
    public String toString() {
        String status = issued ? "Issued to " + issuedTo + " on " + issueDate : "Available";
        return String.format("ID:%-6s | %-30s | %-20s | %s", bookId, title, author, status);
    }

    // For persistence to text file: bookId|title|author|issued|issuedTo|issueDate
    public String toFileLine() {
        String dateStr = (issueDate == null) ? "NULL" : issueDate.format(FMT);
        return bookId + "|" + title + "|" + author + "|" + issued + "|" + issuedTo + "|" + dateStr;
    }

    public static Book fromFileLine(String line) {
        String[] parts = line.split("\\|", -1);
        Book b = new Book(parts[0], parts[1], parts[2]);
        boolean issuedFlag = Boolean.parseBoolean(parts[3]);
        if (issuedFlag) {
            b.issued = true;
            b.issuedTo = parts[4];
            b.issueDate = parts[5].equals("NULL") ? null : LocalDate.parse(parts[5], FMT);
        }
        return b;
    }
}

/* =========================================================
   PERSON HIERARCHY  (Inheritance + Polymorphism)
   ========================================================= */
abstract class Person {
    protected String id;
    protected String name;

    public Person(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() { return id; }
    public String getName() { return name; }

    // Polymorphic method - overridden differently by subclasses
    public abstract String getRole();

    @Override
    public String toString() {
        return String.format("ID:%-6s | %-20s | Role:%s", id, name, getRole());
    }
}

class Member extends Person {
    private List<String> issuedBookIds = new ArrayList<>();
    private double totalFine = 0.0;

    public Member(String id, String name) {
        super(id, name);
    }

    @Override
    public String getRole() { return "Member"; }

    public List<String> getIssuedBookIds() { return issuedBookIds; }
    public double getTotalFine() { return totalFine; }
    public void addFine(double amount) { totalFine += amount; }

    // For persistence: memberId|name|totalFine|book1,book2,...
    public String toFileLine() {
        String books = String.join(",", issuedBookIds);
        return id + "|" + name + "|" + totalFine + "|" + books;
    }

    public static Member fromFileLine(String line) {
        String[] parts = line.split("\\|", -1);
        Member m = new Member(parts[0], parts[1]);
        m.totalFine = Double.parseDouble(parts[2]);
        if (parts.length > 3 && !parts[3].isEmpty()) {
            m.issuedBookIds.addAll(Arrays.asList(parts[3].split(",")));
        }
        return m;
    }
}

/* =========================================================
   LIBRARY CLASS  (Core logic + Collections Framework)
   ========================================================= */
class Library {
    private Map<String, Book> catalog = new HashMap<>();
    private Map<String, Member> members = new HashMap<>();

    private static final int LOAN_PERIOD_DAYS = 14;
    private static final double FINE_PER_DAY = 5.0;

    private static final String BOOKS_FILE = "books.txt";
    private static final String MEMBERS_FILE = "members.txt";

    /* ---------- Book Management ---------- */
    public void addBook(Book book) throws DuplicateIdException {
        if (catalog.containsKey(book.getBookId())) {
            throw new DuplicateIdException("Book ID " + book.getBookId() + " already exists.");
        }
        catalog.put(book.getBookId(), book);
    }

    public void removeBook(String bookId) throws BookNotFoundException {
        if (!catalog.containsKey(bookId)) {
            throw new BookNotFoundException("Book ID " + bookId + " not found.");
        }
        catalog.remove(bookId);
    }

    public void displayAllBooks() {
        if (catalog.isEmpty()) {
            System.out.println("No books in the library.");
            return;
        }
        for (Book b : catalog.values()) {
            System.out.println(b);
        }
    }

    /* ---------- Member Management ---------- */
    public void registerMember(Member member) throws DuplicateIdException {
        if (members.containsKey(member.getId())) {
            throw new DuplicateIdException("Member ID " + member.getId() + " already exists.");
        }
        members.put(member.getId(), member);
    }

    public void displayAllMembers() {
        if (members.isEmpty()) {
            System.out.println("No registered members.");
            return;
        }
        for (Member m : members.values()) {
            System.out.println(m + " | Fine Due: Rs." + m.getTotalFine()
                    + " | Books held: " + m.getIssuedBookIds());
        }
    }

    /* ---------- Search ---------- */
    public List<Book> searchByTitle(String keyword) {
        List<Book> result = new ArrayList<>();
        for (Book b : catalog.values()) {
            if (b.getTitle().toLowerCase().contains(keyword.toLowerCase())) {
                result.add(b);
            }
        }
        return result;
    }

    public List<Book> searchByAuthor(String keyword) {
        List<Book> result = new ArrayList<>();
        for (Book b : catalog.values()) {
            if (b.getAuthor().toLowerCase().contains(keyword.toLowerCase())) {
                result.add(b);
            }
        }
        return result;
    }

    /* ---------- Issue / Return ---------- */
    public void issueBook(String bookId, String memberId)
            throws BookNotFoundException, BookNotAvailableException, MemberNotFoundException {

        Book book = catalog.get(bookId);
        if (book == null) throw new BookNotFoundException("Book ID " + bookId + " not found.");

        Member member = members.get(memberId);
        if (member == null) throw new MemberNotFoundException("Member ID " + memberId + " not found.");

        if (book.isIssued()) {
            throw new BookNotAvailableException("Book \"" + book.getTitle() + "\" is already issued.");
        }

        book.markIssued(memberId);
        member.getIssuedBookIds().add(bookId);
        System.out.println("Book \"" + book.getTitle() + "\" issued to " + member.getName()
                + ". Due date: " + LocalDate.now().plusDays(LOAN_PERIOD_DAYS));
    }

    public double returnBook(String bookId, String memberId)
            throws BookNotFoundException, MemberNotFoundException {

        Book book = catalog.get(bookId);
        if (book == null) throw new BookNotFoundException("Book ID " + bookId + " not found.");

        Member member = members.get(memberId);
        if (member == null) throw new MemberNotFoundException("Member ID " + memberId + " not found.");

        double fine = calculateFine(book.getIssueDate());
        if (fine > 0) member.addFine(fine);

        book.markReturned();
        member.getIssuedBookIds().remove(bookId);

        System.out.println("Book \"" + book.getTitle() + "\" returned by " + member.getName() + ".");
        if (fine > 0) {
            System.out.println("Book was overdue. Fine charged: Rs." + fine);
        } else {
            System.out.println("Returned on time. No fine.");
        }
        return fine;
    }

    /* ---------- Fine Calculation ---------- */
    private double calculateFine(LocalDate issueDate) {
        if (issueDate == null) return 0.0;
        long daysHeld = ChronoUnit.DAYS.between(issueDate, LocalDate.now());
        long overdueDays = daysHeld - LOAN_PERIOD_DAYS;
        return (overdueDays > 0) ? overdueDays * FINE_PER_DAY : 0.0;
    }

    /* ---------- File Handling (Persistence) ---------- */
    public void saveToFile() throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(BOOKS_FILE))) {
            for (Book b : catalog.values()) {
                bw.write(b.toFileLine());
                bw.newLine();
            }
        }
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(MEMBERS_FILE))) {
            for (Member m : members.values()) {
                bw.write(m.toFileLine());
                bw.newLine();
            }
        }
    }

    public void loadFromFile() {
        File bf = new File(BOOKS_FILE);
        if (bf.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(bf))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (!line.trim().isEmpty()) {
                        Book b = Book.fromFileLine(line);
                        catalog.put(b.getBookId(), b);
                    }
                }
            } catch (IOException e) {
                System.out.println("Warning: could not load books file - " + e.getMessage());
            }
        }

        File mf = new File(MEMBERS_FILE);
        if (mf.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(mf))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (!line.trim().isEmpty()) {
                        Member m = Member.fromFileLine(line);
                        members.put(m.getId(), m);
                    }
                }
            } catch (IOException e) {
                System.out.println("Warning: could not load members file - " + e.getMessage());
            }
        }
    }
}

/* =========================================================
   MAIN CLASS - Console Menu Driven Application
   ========================================================= */
public class LibraryManagementSystem {

    private static Scanner sc = new Scanner(System.in);
    private static Library library = new Library();

    public static void main(String[] args) {
        library.loadFromFile();
        System.out.println("===== Library Management System =====");

        boolean running = true;
        while (running) {
            printMenu();
            String choice = sc.nextLine().trim();

            try {
                switch (choice) {
                    case "1": addBook(); break;
                    case "2": removeBook(); break;
                    case "3": registerMember(); break;
                    case "4": issueBook(); break;
                    case "5": returnBook(); break;
                    case "6": searchBooks(); break;
                    case "7": library.displayAllBooks(); break;
                    case "8": library.displayAllMembers(); break;
                    case "9":
                        library.saveToFile();
                        System.out.println("Data saved. Exiting... Goodbye!");
                        running = false;
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            } catch (BookNotFoundException | BookNotAvailableException
                     | MemberNotFoundException | DuplicateIdException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (IOException e) {
                System.out.println("File error: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Unexpected error: " + e.getMessage());
            }
            System.out.println();
        }
        sc.close();
    }

    private static void printMenu() {
        System.out.println("---------------------------------------");
        System.out.println("1. Add Book");
        System.out.println("2. Remove Book");
        System.out.println("3. Register Member");
        System.out.println("4. Issue Book");
        System.out.println("5. Return Book");
        System.out.println("6. Search Books");
        System.out.println("7. Display All Books");
        System.out.println("8. Display All Members");
        System.out.println("9. Save & Exit");
        System.out.println("---------------------------------------");
        System.out.print("Enter your choice: ");
    }

    private static void addBook() throws DuplicateIdException {
        System.out.print("Book ID: ");
        String id = sc.nextLine().trim();
        System.out.print("Title: ");
        String title = sc.nextLine().trim();
        System.out.print("Author: ");
        String author = sc.nextLine().trim();
        library.addBook(new Book(id, title, author));
        System.out.println("Book added successfully.");
    }

    private static void removeBook() throws BookNotFoundException {
        System.out.print("Enter Book ID to remove: ");
        String id = sc.nextLine().trim();
        library.removeBook(id);
        System.out.println("Book removed successfully.");
    }

    private static void registerMember() throws DuplicateIdException {
        System.out.print("Member ID: ");
        String id = sc.nextLine().trim();
        System.out.print("Name: ");
        String name = sc.nextLine().trim();
        library.registerMember(new Member(id, name));
        System.out.println("Member registered successfully.");
    }

    private static void issueBook() throws BookNotFoundException, BookNotAvailableException, MemberNotFoundException {
        System.out.print("Book ID: ");
        String bookId = sc.nextLine().trim();
        System.out.print("Member ID: ");
        String memberId = sc.nextLine().trim();
        library.issueBook(bookId, memberId);
    }

    private static void returnBook() throws BookNotFoundException, MemberNotFoundException {
        System.out.print("Book ID: ");
        String bookId = sc.nextLine().trim();
        System.out.print("Member ID: ");
        String memberId = sc.nextLine().trim();
        library.returnBook(bookId, memberId);
    }

    private static void searchBooks() {
        System.out.print("Search by (1) Title or (2) Author: ");
        String mode = sc.nextLine().trim();
        System.out.print("Enter keyword: ");
        String keyword = sc.nextLine().trim();

        List<Book> results = mode.equals("2")
                ? library.searchByAuthor(keyword)
                : library.searchByTitle(keyword);

        if (results.isEmpty()) {
            System.out.println("No matching books found.");
        } else {
            for (Book b : results) System.out.println(b);
        }
    }
}