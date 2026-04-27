# Week 8 - Servlet for Book Retrieval

## Aim

Develop a servlet that retrieves book information from a database and sends it to the content frame.

## Files Used

- `pom.xml` - Maven project configuration
- `src/main/webapp/index.html` - page containing link and content iframe
- `src/main/java/com/bookstore/BookServlet.java` - servlet that creates sample database records and displays them

## Concepts Involved

### Servlet

A servlet is a Java class that runs on a server and handles client requests. In web applications, servlets commonly handle HTTP requests and generate HTTP responses.

### HTTP Request and Response

When a browser asks for a resource, it sends an HTTP request. The server processes the request and returns an HTTP response.

In this lab:

- Browser requests `/books`
- `BookServlet` handles the request
- Servlet reads book data from database
- Servlet sends HTML table as response

### Servlet Lifecycle

The servlet lifecycle is managed by Tomcat.

Main methods:

- `init()` - called once when servlet is created
- `doGet()` - called for HTTP GET requests
- `doPost()` - called for HTTP POST requests
- `destroy()` - called when servlet is removed from service

In this lab:

- `init()` creates and fills the H2 database table
- `doGet()` retrieves and displays books

### `@WebServlet`

`@WebServlet("/books")` maps the servlet to the URL `/books`. When the browser opens this URL, Tomcat calls the servlet.

### JDBC

JDBC stands for Java Database Connectivity. It is used by Java programs to connect with databases.

Important JDBC classes used:

- `Connection` - connection to the database
- `DriverManager` - creates database connection
- `Statement` - executes SQL queries
- `ResultSet` - stores query output

### H2 Database

H2 is a lightweight Java database. This lab uses an in-memory H2 database so no external database installation is required.

Database URL used:

```text
jdbc:h2:mem:bookstore;DB_CLOSE_DELAY=-1
```

`mem` means in-memory database. `DB_CLOSE_DELAY=-1` keeps the database available while the application is running.

### Content Frame

The lab requirement says data should be sent to the content frame. This implementation uses an HTML `iframe` named `contentFrame`. When the link is clicked, servlet output loads inside that frame.

## Setup Steps

### Required Software

- Java JDK 8 or 11
- Maven
- Apache Tomcat 9
- Eclipse IDE or IntelliJ IDEA

### Run in Eclipse

1. Open Eclipse.
2. Choose `File > Import`.
3. Select `Existing Maven Projects`.
4. Browse to `Week-08-Book-Retrieval-Servlet`.
5. Finish import and wait for Maven dependencies to download.
6. Right-click the project.
7. Select `Run As > Run on Server`.
8. Choose Apache Tomcat 9.
9. Open:

```text
http://localhost:8080/Week-08-Book-Retrieval-Servlet/
```

10. Click `Load Books from Servlet`.

### Run Using Maven

If Maven is installed:

```bash
mvn clean package
```

Deploy the generated WAR file from `target/` to Tomcat.

## Expected Output

When `Load Books from Servlet` is clicked, the content frame displays a table with:

- Book title
- Author
- Price

## Viva Questions and Answers

### What is a servlet?

A servlet is a server-side Java program used to handle web requests and generate responses.

### What is `doGet()`?

`doGet()` handles HTTP GET requests, usually used to read or display data.

### What is `init()`?

`init()` is called once when the servlet is initialized.

### What is JDBC?

JDBC is a Java API used to connect and execute SQL queries on databases.

### What is `ResultSet`?

`ResultSet` stores the data returned by a SQL SELECT query.

### What is `@WebServlet`?

It maps a servlet class to a URL pattern.

### Why is H2 used?

H2 is simple and runs in memory, so the lab does not need separate database setup.

### What is the difference between GET and POST?

GET is mainly used to request data. POST is mainly used to submit data that changes server state.

### What is a content frame?

It is a frame or iframe area where selected content is loaded without changing the whole page.

## From Blank System Using VS Code

Required software:

- Visual Studio Code
- Java JDK 8 or 11
- Maven
- Apache Tomcat 9
- Extension Pack for Java
- Maven for Java
- Community Server Connectors or Tomcat server extension

Create this structure:

```text
Week-08-Book-Retrieval-Servlet/
  pom.xml
  src/main/java/com/bookstore/BookServlet.java
  src/main/webapp/index.html
```

Steps:

1. Create the folder and open it in VS Code.
2. Create `pom.xml` with `<packaging>war</packaging>`.
3. Add dependencies for `javax.servlet-api` and `h2`.
4. Create `src/main/java/com/bookstore/BookServlet.java`.
5. Add `package com.bookstore;`.
6. Extend `HttpServlet` and map it using `@WebServlet("/books")`.
7. Use `init()` to create sample H2 table.
8. Use `doGet()` to read books and print an HTML table.
9. Create `src/main/webapp/index.html` with a link or iframe pointing to `books`.
10. Build using `mvn clean package`.
11. Deploy the WAR from `target/` to Tomcat 9.



# debugging steps (same for week-9):
1. Open Eclipse
2. Import this Maven project (file --> import)
3. Run As Server (right click --> properties --> Maven --> project facets --> convert to facet form --> select Java, Dynamic web module --> apply and close)
4. Select tomcat server (v9) --> restart server
5. If still not running: right click --> Maven --> update project --> force update/snapshot releases
6. properties --> deployment assembly --> add --> java build path entries --> maven dependencies --> apply and close --> clean and restart
7. bottom window --> right click on server --> add and remove