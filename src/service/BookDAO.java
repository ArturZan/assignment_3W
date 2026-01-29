package service;
import model.Author;
import model.Book;
import model.EBook;
import model.printedBook;
import utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class BookDAO {
    public void addEBook(EBook bookToAdd) {
        String insertQuery = "insert into books(title, isbn,author_id, publish_year, book_type, download_url, file_size) values(?,?,?,?,'EBOOK',?,?)";

        try (Connection dbConnection = DatabaseConnection.getConnection();
        PreparedStatement statement = dbConnection.prepareStatement(insertQuery)) {
            statement.setString(1, bookToAdd.getTitle());
            statement.setString(2, bookToAdd.getIsbn());
            statement.setInt(3, bookToAdd.getAuthor().getId());
            statement.setInt(4, bookToAdd.getYear());
            statement.setString(5, bookToAdd.getDownloadURL());
            statement.setDouble(6, bookToAdd.getFileSize());
            statement.executeUpdate();
        } catch (SQLException sqlException) {
            System.err.println(sqlException.getMessage());
        }
    }

   public void addPrintedBook(printedBook bookToAdd) {
        String insertQuery = "insert into books(title, isbn, author_id, publish_year, book_type, shelf_location, weight, available) " +
                "values(?, ?, ?, ?, 'PRINTED', ?, ?, ?)";

        try (Connection dbConnection = DatabaseConnection.getConnection();
        PreparedStatement statement = dbConnection.prepareStatement(insertQuery)){
            statement.setString(1, bookToAdd.getTitle());
            statement.setString(2, bookToAdd.getIsbn());
            statement.setInt(3, bookToAdd.getAuthor().getId());
            statement.setInt(4, bookToAdd.getYear());
            statement.setString(5, bookToAdd.getShelfLocation());
            statement.setDouble(6, bookToAdd.getWeight());
            statement.setBoolean(7, bookToAdd.isAvailable());
            statement.executeUpdate();
        } catch (SQLException sqlException) {
            System.err.println(sqlException.getMessage());
        }
   }

    public Book getBookByID(int bookId) {
        String selectQuery = "select * from books where id = ?";
        try (Connection dbConnection = DatabaseConnection.getConnection();
            PreparedStatement statement = dbConnection.prepareStatement(selectQuery))  {
            statement.setInt(1, bookId);
            ResultSet queryResult = statement.executeQuery();


            if (queryResult.next()) {
                String typeOfBook = queryResult.getString("book_type");

                if ("EBOOK".equals(typeOfBook)) {
                    return createEBookFromResultSet(queryResult);
                } else if("PRINTED".equals((typeOfBook))) {
                    return createPrintedBookFromResultSet(queryResult);
                }
            } else {
                return null;
            }
        } catch (SQLException sqlException) {
            System.err.println("Error" + sqlException.getMessage());
            return null;
        }
        return null;
    }

    public EBook createEBookFromResultSet(ResultSet resultData) throws SQLException {
        int bookId = resultData.getInt("id");
        String bookTitle = resultData.getString("title");
        String bookIsbn = resultData.getString("isbn");
        int authorIdentifier = resultData.getInt("author_id");
        int publicationYear = resultData.getInt("publish_year");
        String urlForDownload = resultData.getString("download_url");
        double sizeOfFile = resultData.getDouble("file_size");

        AuthorDAO authorDataAccess = new AuthorDAO();
        Author bookAuthor = authorDataAccess.getAuthorByID(authorIdentifier);

        EBook electronicBook = new EBook(bookId, bookTitle, bookAuthor, publicationYear, bookIsbn, sizeOfFile, urlForDownload);
        return electronicBook;
    }

    public printedBook createPrintedBookFromResultSet(ResultSet resultData) throws SQLException {
        int bookId = resultData.getInt("id");
        String bookTitle = resultData.getString("title");
        String bookIsbn = resultData.getString("isbn");
        int authorIdentifier = resultData.getInt("author_id");
        int publicationYear = resultData.getInt("publish_year");
        String locationOnShelf = resultData.getString("shelf_location");
        double bookWeight = resultData.getDouble("weight");

        AuthorDAO authorDataAccess = new AuthorDAO();
        Author bookAuthor = authorDataAccess.getAuthorByID(authorIdentifier);
        printedBook physicalBook = new printedBook(bookId, bookTitle, bookAuthor, publicationYear, bookIsbn, locationOnShelf, bookWeight);
        return physicalBook;
    }

    public List<Book> getAllBooks() {
        String selectAllQuery = "select * from books";
        List<Book> bookCollection = new ArrayList<>();

        try (Connection dbConnection = DatabaseConnection.getConnection();
        PreparedStatement statement = dbConnection.prepareStatement(selectAllQuery)) {
            ResultSet queryResult = statement.executeQuery();
            while (queryResult.next()) {
                String typeOfBook = queryResult.getString("book_type");

                if ("EBOOK".equals(typeOfBook)) {
                    bookCollection.add(createEBookFromResultSet(queryResult));
                } else if ("PRINTED".equals(typeOfBook)) {
                    bookCollection.add(createPrintedBookFromResultSet(queryResult));
                }
            }
        } catch (SQLException sqlException) {
            System.err.println(sqlException.getMessage());
        }
        return bookCollection;

    }

    public Book getBookByISBN(String isbnCode) {
        String selectQuery = "select * from books where isbn = ?";
        try (Connection dbConnection = DatabaseConnection.getConnection();
             PreparedStatement statement = dbConnection.prepareStatement(selectQuery)) {
            statement.setString(1, isbnCode);
            ResultSet queryResult = statement.executeQuery();
            if (queryResult.next()) {
                String typeOfBook = queryResult.getString("book_type");
                if ("EBOOK".equals(typeOfBook)) {
                    return createEBookFromResultSet(queryResult);
                } else if ("PRINTED".equals(typeOfBook)) {
                    return createPrintedBookFromResultSet(queryResult);
                }
            }
        } catch (SQLException sqlException) {
            System.err.println(sqlException.getMessage());
        }
        return null;
    }

    public void updateBookByID(int bookId, String updatedTitle, String updatedIsbn, int updatedAuthorId, int updatedPublishYear, double updatedFileSize, String updatedDownloadUrl, String updatedShelfLocation, double updatedWeight) {
        String updateQuery = "update table books set title = ?,isbn =?, author_id = ?, publish_year = ?, file_size=?,download_url = ?, shelf_locaion=?, weight = ?  where id = ?";
        String selectTypeQuery = "select book_type from books where id = ?";
        try (Connection dbConnection = DatabaseConnection.getConnection();
        PreparedStatement statementCheckId = dbConnection.prepareStatement(selectTypeQuery);
        PreparedStatement statementUpdate = dbConnection.prepareStatement(updateQuery);) {

            String categoryOfBook;
            statementCheckId.setInt(1, bookId);
            ResultSet queryResult = statementCheckId.executeQuery();
            if (queryResult.next()) {
                categoryOfBook = queryResult.getString("book_type");
                if ("EBOOK".equals(categoryOfBook)) {
                    statementUpdate.setString(1, updatedTitle);
                    statementUpdate.setString(2, updatedIsbn);
                    statementUpdate.setInt(3, updatedAuthorId);
                    statementUpdate.setInt(4, updatedPublishYear);
                    statementUpdate.setDouble(5, updatedFileSize);
                    statementUpdate.setString(6, updatedDownloadUrl);
                    statementUpdate.setNull(7, Types.DOUBLE);
                    statementUpdate.setNull(8, Types.VARCHAR); //null because ebook does not have shelflocation and weight

            
                    statementUpdate.setInt(9, bookId);
                }
                else if ("PRINTEDBOOK".equals(categoryOfBook)) {
                    statementUpdate.setString(1, updatedTitle);
                    statementUpdate.setString(2, updatedIsbn);
                    statementUpdate.setInt(3, updatedAuthorId);
                    statementUpdate.setInt(4, updatedPublishYear);
                    statementUpdate.setNull(5, Types.DOUBLE); //null because printed book does not have filesize and download url
                    statementUpdate.setNull(6, Types.VARCHAR);
                    statementUpdate.setString(7, updatedShelfLocation);
                    statementUpdate.setDouble(8, updatedWeight);
        
                    statementUpdate.setInt(9, bookId);
                }
                else {
                    return;
                }
            }
            int affectedRows = statementUpdate.executeUpdate();
            if(affectedRows > 0) {
                System.out.println("Book updated successfully");
            } else {
                System.out.println("There is no book with this id");
            }
        } catch (SQLException sqlException) {
            System.err.println(sqlException.getMessage());
        }
    }

    public void deleteBookById(int bookId) {
        String deleteQuery = "delete from books where id = ?";

        try (Connection dbConnection = DatabaseConnection.getConnection();
        PreparedStatement statement = dbConnection.prepareStatement(deleteQuery)) {
            statement.setInt(1, bookId);
            statement.executeUpdate();
        } catch (SQLException sqlException) {
            System.err.println(sqlException.getMessage());
        }
    }
}
