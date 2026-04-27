package com.test;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.util.List;
import java.util.Scanner;

public class App {
    public static void main(String[] args) {

        var cfgUrl = App.class.getClassLoader().getResource("hibernate.cfg.xml");
        if (cfgUrl == null) {
            throw new IllegalStateException("hibernate.cfg.xml not found on classpath");
        }

        SessionFactory factory = new Configuration()
            .configure(cfgUrl)
            .buildSessionFactory();

        Scanner sc = new Scanner(System.in);

        try {
            boolean exit = false;

            while (!exit) {
                System.out.println("\n=====================");
                System.out.println("1. Create Book");
                System.out.println("2. Read Book by ID");
                System.out.println("3. Read All Books");
                System.out.println("4. Update Book by ID");
                System.out.println("5. Delete Book by ID");
                System.out.println("6. Exit");
                System.out.print("Enter your choice: ");

                int choice = sc.nextInt();
                sc.nextLine(); 

                switch (choice) {
                    case 1 -> { // CREATE
                        System.out.print("Enter book title: ");
                        String title = sc.nextLine();
                        System.out.print("Enter book author: ");
                        String author = sc.nextLine();

                        Session session = factory.openSession();
                        session.beginTransaction();

                        Book b = new Book();
                        b.setTitle(title);
                        b.setAuthor(author);

                        session.persist(b);
                        session.getTransaction().commit();
                        session.close();

                        System.out.println("Inserted successfully! ID = " + b.getId());
                    }
                    case 2 -> { // READ by ID
                        System.out.print("Enter book ID to read: ");
                        int id = sc.nextInt();
                        sc.nextLine();

                        Session session = factory.openSession();
                        session.beginTransaction();

                        Book b = session.get(Book.class, id);
                        if (b != null) {
                            System.out.println("ID: " + b.getId());
                            System.out.println("Title: " + b.getTitle());
                            System.out.println("Author: " + b.getAuthor());
                        } else {
                            System.out.println("Book not found.");
                        }

                        session.getTransaction().commit();
                        session.close();
                    }
                    case 3 -> { // READ ALL
                        Session session = factory.openSession();
                        session.beginTransaction();

                        List<Book> books = session.createQuery("from Book", Book.class).getResultList();
                        if (!books.isEmpty()) {
                            System.out.println("\n--- ALL BOOKS ---");
                            for (Book b : books) {
                                System.out.println("ID: " + b.getId() + ", Title: " + b.getTitle() + ", Author: " + b.getAuthor());
                            }
                        } else {
                            System.out.println("No books found.");
                        }

                        session.getTransaction().commit();
                        session.close();
                    }
                    case 4 -> { // UPDATE
                        System.out.print("Enter book ID to update: ");
                        int id = sc.nextInt();
                        sc.nextLine();

                        Session session = factory.openSession();
                        session.beginTransaction();

                        Book b = session.get(Book.class, id);
                        if (b != null) {
                            System.out.print("Enter new title: ");
                            String newTitle = sc.nextLine();
                            System.out.print("Enter new author: ");
                            String newAuthor = sc.nextLine();

                            b.setTitle(newTitle);
                            b.setAuthor(newAuthor);

                            session.merge(b);
                            System.out.println("Updated successfully!");
                        } else {
                            System.out.println("Book not found.");
                        }

                        session.getTransaction().commit();
                        session.close();
                    }
                    case 5 -> { // DELETE
                        System.out.print("Enter book ID to delete: ");
                        int id = sc.nextInt();
                        sc.nextLine();

                        Session session = factory.openSession();
                        session.beginTransaction();

                        Book b = session.get(Book.class, id);
                        if (b != null) {
                            session.remove(b);
                            System.out.println("Deleted successfully!");
                        } else {
                            System.out.println("Book not found.");
                        }

                        session.getTransaction().commit();
                        session.close();
                    }
                    case 6 -> exit = true; // EXIT
                    default -> System.out.println("Invalid choice. Try again.");
                }
            }

        } finally {
            factory.close();
            sc.close();
        }
    }
}