package com.test;

public class App {
    public static void main(String[] args) {
        try {
            ProductServer.main(args);
        } catch (Exception ex) {
            System.err.println("Failed to start server: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}