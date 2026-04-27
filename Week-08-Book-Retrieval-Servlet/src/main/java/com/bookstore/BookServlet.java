package com.bookstore;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/books")
public class BookServlet extends HttpServlet {
    private static final String DB_URL = "jdbc:h2:mem:bookstore;DB_CLOSE_DELAY=-1";

    @Override
    public void init() throws ServletException {
        try {
            Class.forName("org.h2.Driver");
            try (Connection connection = DriverManager.getConnection(DB_URL);
                 Statement statement = connection.createStatement()) {
                statement.execute("CREATE TABLE IF NOT EXISTS books (id INT PRIMARY KEY, title VARCHAR(80), author VARCHAR(80), price DOUBLE)");
                statement.execute("MERGE INTO books KEY(id) VALUES (1, 'Core Java', 'Herbert Schildt', 650)");
                statement.execute("MERGE INTO books KEY(id) VALUES (2, 'Web Technologies', 'Chris Bates', 580)");
                statement.execute("MERGE INTO books KEY(id) VALUES (3, 'Database Systems', 'Elmasri and Navathe', 720)");
            }
        } catch (ClassNotFoundException | SQLException exception) {
            throw new ServletException("Database initialization failed", exception);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");

        try (PrintWriter out = response.getWriter();
             Connection connection = DriverManager.getConnection(DB_URL);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT title, author, price FROM books")) {

            out.println("<html><body style='font-family:Arial'>");
            out.println("<h2>Books Retrieved from Database</h2>");
            out.println("<table border='1' cellpadding='8'>");
            out.println("<tr><th>Title</th><th>Author</th><th>Price</th></tr>");

            while (resultSet.next()) {
                out.println("<tr>");
                out.println("<td>" + resultSet.getString("title") + "</td>");
                out.println("<td>" + resultSet.getString("author") + "</td>");
                out.println("<td>Rs. " + resultSet.getDouble("price") + "</td>");
                out.println("</tr>");
            }

            out.println("</table></body></html>");
        } catch (SQLException exception) {
            response.getWriter().println("Unable to retrieve books: " + exception.getMessage());
        }
    }
}
