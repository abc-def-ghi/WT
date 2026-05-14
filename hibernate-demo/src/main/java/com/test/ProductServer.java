package com.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.hibernate.SessionFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class ProductServer {

    private static final int PORT = 8080;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static void main(String[] args) throws IOException {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        ProductRepository repository = new ProductRepository(sessionFactory);

        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/api/products", new ProductApiHandler(repository));
        server.createContext("/", new StaticFileHandler());
        server.setExecutor(null);
        server.start();

        System.out.println("Product server running at http://localhost:" + PORT);
    }

    private static class ProductApiHandler implements HttpHandler {

        private final ProductRepository repository;

        ProductApiHandler(ProductRepository repository) {
            this.repository = repository;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            if (path.equals("/api/products") || path.equals("/api/products/")) {
                handleCollection(exchange, method);
                return;
            }

            if (path.startsWith("/api/products/")) {
                handleItem(exchange, method, path);
                return;
            }

            sendJson(exchange, 404, Map.of("error", "Not found"));
        }

        private void handleCollection(HttpExchange exchange, String method) throws IOException {
            switch (method) {
                case "GET" -> {
                    List<Product> products = repository.findAll();
                    sendJson(exchange, 200, products);
                }
                case "POST" -> {
                    ProductPayload payload = readPayload(exchange);
                    String error = validatePayload(payload);
                    if (error != null) {
                        sendJson(exchange, 400, Map.of("error", error));
                        return;
                    }
                    Product product = new Product();
                    product.setName(payload.name);
                    product.setCategory(payload.category);
                    product.setPrice(payload.price);
                    Product saved = repository.create(product);
                    sendJson(exchange, 201, saved);
                }
                default -> sendJson(exchange, 405, Map.of("error", "Method not allowed"));
            }
        }

        private void handleItem(HttpExchange exchange, String method, String path) throws IOException {
            Integer id = parseId(path);
            if (id == null) {
                sendJson(exchange, 400, Map.of("error", "Invalid product id"));
                return;
            }

            switch (method) {
                case "GET" -> {
                    Product product = repository.findById(id);
                    if (product == null) {
                        sendJson(exchange, 404, Map.of("error", "Product not found"));
                        return;
                    }
                    sendJson(exchange, 200, product);
                }
                case "PUT" -> {
                    ProductPayload payload = readPayload(exchange);
                    String error = validatePayload(payload);
                    if (error != null) {
                        sendJson(exchange, 400, Map.of("error", error));
                        return;
                    }
                    Product updated = new Product();
                    updated.setName(payload.name);
                    updated.setCategory(payload.category);
                    updated.setPrice(payload.price);
                    Product saved = repository.update(id, updated);
                    if (saved == null) {
                        sendJson(exchange, 404, Map.of("error", "Product not found"));
                        return;
                    }
                    sendJson(exchange, 200, saved);
                }
                case "DELETE" -> {
                    boolean deleted = repository.delete(id);
                    if (!deleted) {
                        sendJson(exchange, 404, Map.of("error", "Product not found"));
                        return;
                    }
                    sendJson(exchange, 200, Map.of("status", "deleted"));
                }
                default -> sendJson(exchange, 405, Map.of("error", "Method not allowed"));
            }
        }

        private ProductPayload readPayload(HttpExchange exchange) throws IOException {
            try (InputStream body = exchange.getRequestBody()) {
                return MAPPER.readValue(body, ProductPayload.class);
            }
        }

        private String validatePayload(ProductPayload payload) {
            if (payload == null) {
                return "Request body is required";
            }
            if (payload.name == null || payload.name.trim().length() < 2) {
                return "Name must be at least 2 characters";
            }
            if (payload.category == null || payload.category.trim().length() < 2) {
                return "Category must be at least 2 characters";
            }
            if (payload.price == null || payload.price.compareTo(BigDecimal.ZERO) <= 0) {
                return "Price must be greater than 0";
            }
            return null;
        }

        private Integer parseId(String path) {
            String[] parts = path.split("/");
            if (parts.length < 4) {
                return null;
            }
            try {
                return Integer.parseInt(parts[3]);
            } catch (NumberFormatException ex) {
                return null;
            }
        }
    }

    private static class StaticFileHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) {
                path = "/index.html";
            }
            if (path.contains("..")) {
                sendText(exchange, 400, "Bad request");
                return;
            }

            String resourcePath = "public" + path;
            InputStream resource = ProductServer.class.getClassLoader().getResourceAsStream(resourcePath);
            if (resource == null) {
                sendText(exchange, 404, "Not found");
                return;
            }

            byte[] bytes = resource.readAllBytes();
            exchange.getResponseHeaders().set("Content-Type", contentType(path));
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream output = exchange.getResponseBody()) {
                output.write(bytes);
            }
        }

        private String contentType(String path) {
            if (path.endsWith(".css")) {
                return "text/css; charset=UTF-8";
            }
            if (path.endsWith(".js")) {
                return "application/javascript; charset=UTF-8";
            }
            if (path.endsWith(".svg")) {
                return "image/svg+xml";
            }
            if (path.endsWith(".png")) {
                return "image/png";
            }
            return "text/html; charset=UTF-8";
        }
    }

    private static void sendJson(HttpExchange exchange, int status, Object body) throws IOException {
        byte[] json = MAPPER.writeValueAsBytes(body);
        Headers headers = exchange.getResponseHeaders();
        headers.set("Content-Type", "application/json; charset=UTF-8");
        headers.set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(status, json.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(json);
        }
    }

    private static void sendText(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }

    private static class ProductPayload {
        public String name;
        public String category;
        public BigDecimal price;
    }
}
