import java.util.ArrayList;
import java.util.List;

class InvalidInputException extends Exception {
    public InvalidInputException(String message) {
        super(message);
    }
}

class ResourceNotFoundException extends Exception {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}

class DuplicateResourceException extends Exception {
    public DuplicateResourceException(String message) {
        super(message);
    }
}

interface Borrowable {
    void borrow();
    void returnItem();
    boolean isAvailable();
}

interface DigitalAccess {
    String getDownloadURL();
    double getFileSize();
}

class Author {
    private int id;
    private String name;
    private int birthYear;
    private String nationality;

    public Author(int id, String name, int birthYear, String nationality) {
        this.id = id;
        this.name = name;
        this.birthYear = birthYear;
        this.nationality = nationality;
    }

    public String getName() { return name; }
    public int getBirthYear() { return birthYear; }
    public String getNationality() { return nationality; }
    public void setId(int id) { this.id = id; }
}

abstract class Book {
    private int id;
    private String title;
    private Author author;
    private int year;
    private String isbn;

    public Book(int id, String title, Author author, int year, String isbn) {
        this.id = id;
        setTitle(title);
        this.author = author;
        setYear(year);
        this.isbn = isbn;
    }

    public abstract double calculateLateFee(int days);
    public abstract String getAccessInstructions();

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
        this.title = title;
    }

    public int getYear() { return year; }
    public void setYear(int year) {
        if (year > 2026) {
            throw new IllegalArgumentException("Year cannot be in the future");
        }
        this.year = year;
    }

    public Author getAuthor() { return author; }
    public String getIsbn() { return isbn; }
}

class EBook extends Book implements Borrowable, DigitalAccess {
    private double fileSize;
    private String downloadUrl;
    private boolean available = true;

    public EBook(int id, String title, Author author, int year, String isbn, double fileSize, String downloadUrl) {
        super(id, title, author, year, isbn);
        setFileSize(fileSize);
        this.downloadUrl = downloadUrl;
    }

    @Override
    public double calculateLateFee(int days) {
        return days * 0.25;
    }

    @Override
    public String getAccessInstructions() {
        return "Download from portal";
    }

    public void setFileSize(double fileSize) {
        if (fileSize < 0) throw new IllegalArgumentException("File size cannot be negative");
        this.fileSize = fileSize;
    }

    @Override public String getDownloadURL() { return downloadUrl; }
    @Override public double getFileSize() { return fileSize; }

    @Override
    public void borrow() {
        if(available) available = false;
    }
    @Override
    public void returnItem() {
        available = true;
    }
    @Override
    public boolean isAvailable() {
        return available;
    }
}

class PrintedBook extends Book implements Borrowable {
    private String shelfLocation;
    private double weight;
    private boolean available = true;

    public PrintedBook(int id, String title, Author author, int year, String isbn, String shelfLocation, double weight) {
        super(id, title, author, year, isbn);
        this.shelfLocation = shelfLocation;
        this.weight = weight;
    }

    @Override
    public double calculateLateFee(int days) {
        return days * 0.50;
    }

    @Override
    public String getAccessInstructions() {
        return "Go to shelf: " + shelfLocation;
    }

    @Override public void borrow() { available = false; }
    @Override public void returnItem() { available = true; }
    @Override public boolean isAvailable() { return available; }
}

class AuthorService {
    private List<Author> authors = new ArrayList<>();
    private int idCounter = 1;

    public void createAuthor(Author author) {
        author.setId(idCounter++);
        authors.add(author);
    }

    public List<Author> getAllAuthors() {
        return authors;
    }
}

class BookService {
    private List<Book> books = new ArrayList<>();
    private int idCounter = 1;

    public void createEBook(EBook book) throws InvalidInputException {
        validateBook(book);
        book.setId(idCounter++);
        books.add(book);
    }

    public void createPrintedBook(PrintedBook book) throws InvalidInputException {
        validateBook(book);
        book.setId(idCounter++);
        books.add(book);
    }

    private void validateBook(Book book) throws InvalidInputException {
        if (book.getTitle() == null || book.getTitle().isEmpty()) {
            throw new InvalidInputException("Book title cannot be empty");
        }
        if (book instanceof EBook && ((EBook) book).getFileSize() < 0) {
            throw new InvalidInputException("File size cannot be negative");
        }
    }

    public List<Book> getAllBooks() {
        return books;
    }

    public Book getBookByID(int id) throws ResourceNotFoundException {
        for (Book b : books) {
            if (b.getId() == id) {
                return b;
            }
        }
        throw new ResourceNotFoundException("Book with ID " + id + " not found");
    }

    public void deleteBookById(int id) throws ResourceNotFoundException {
        Book book = getBookByID(id);
        books.remove(book);
    }
}

public class Main {
    public static void main(String[] args) {
        BookService bookService = new BookService();
        AuthorService authorService = new AuthorService();

        try {
            System.out.println("--- Polymorphism Test ---");
            Author author1 = new Author(0, "George Orwell", 1903, "British");
            authorService.createAuthor(author1);

            Book book1 = new EBook(0, "1984", author1, 1949, "12345", 2.5, "site.com");
            Book book2 = new PrintedBook(0, "Animal Farm", author1, 1945, "67890", "A1", 0.5);

            bookService.createEBook((EBook) book1);
            bookService.createPrintedBook((PrintedBook) book2);

            System.out.println("Book 1 Fee (10 days): " + book1.calculateLateFee(10));
            System.out.println("Book 2 Fee (10 days): " + book2.calculateLateFee(10));
            System.out.println("Book 1 Access: " + book1.getAccessInstructions());
            System.out.println("Book 2 Access: " + book2.getAccessInstructions());

            System.out.println("\n--- Interface Test ---");
            if (book1 instanceof Borrowable) {
                ((Borrowable) book1).borrow();
                System.out.println("Book 1 is available: " + ((Borrowable) book1).isAvailable());
                ((Borrowable) book1).returnItem();
                System.out.println("Book 1 is available after return: " + ((Borrowable) book1).isAvailable());
            }

            System.out.println("\n--- CRUD Test ---");
            List<Book> allBooks = bookService.getAllBooks();
            System.out.println("Total books: " + allBooks.size());

            for (Book b : allBooks) {
                System.out.println("ID: " + b.getId() + ", Title: " + b.getTitle());
            }

            Book foundBook = bookService.getBookByID(1);
            System.out.println("Found book: " + foundBook.getTitle());

            foundBook.setTitle("1984 (Updated)");
            System.out.println("Updated title: " + foundBook.getTitle());

            bookService.deleteBookById(2);
            System.out.println("Book with ID 2 deleted. Remaining: " + bookService.getAllBooks().size());

            System.out.println("\n--- Exception Test ---");
            try {
                bookService.getBookByID(99);
            } catch (ResourceNotFoundException e) {
                System.out.println("Caught exception: " + e.getMessage());
            }

            try {
                EBook badBook = new EBook(0, "", author1, 2020, "000", 1.0, "url");
                bookService.createEBook(badBook);
            } catch (InvalidInputException | IllegalArgumentException e) {
                System.out.println("Caught exception: " + e.getMessage());
            }

            System.out.println("\n--- Composition Test ---");
            System.out.println("Book: " + book1.getTitle() + " is written by " + book1.getAuthor().getName());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}