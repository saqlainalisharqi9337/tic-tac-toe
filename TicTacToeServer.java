
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;

public class TicTacToeServer {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(
                new InetSocketAddress(8080), 0);

        server.createContext("/api/status", TicTacToeServer::handleStatus);
        server.createContext("/", TicTacToeServer::handleStaticFile);

        server.start();
        System.out.println("Tic Tac Toe running at http://localhost:8080");
    }

    private static void handleStatus(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "text/plain; charset=UTF-8",
                    "Method not allowed".getBytes(StandardCharsets.UTF_8));
            return;
        }

        byte[] response =
                "{\"message\":\"Tic Tac Toe Java server is running\"}"
                        .getBytes(StandardCharsets.UTF_8);
        sendResponse(exchange, 200, "application/json; charset=UTF-8", response);
    }

    private static void handleStaticFile(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "text/plain; charset=UTF-8",
                    "Method not allowed".getBytes(StandardCharsets.UTF_8));
            return;
        }

        String requestPath = exchange.getRequestURI().getPath();
        Path file;
        String contentType;

        if ("/".equals(requestPath) || "/tic_tac-toe.html".equals(requestPath)) {
            file = Path.of("tic_tac-toe.html");
            contentType = "text/html; charset=UTF-8";
        } else if ("/tic tac toe.css".equals(requestPath)) {
            file = Path.of("tic tac toe.css");
            contentType = "text/css; charset=UTF-8";
        } else {
            sendResponse(exchange, 404, "text/plain; charset=UTF-8",
                    "Not found".getBytes(StandardCharsets.UTF_8));
            return;
        }

        if (!Files.isRegularFile(file)) {
            sendResponse(exchange, 404, "text/plain; charset=UTF-8",
                    "Not found".getBytes(StandardCharsets.UTF_8));
            return;
        }

        sendResponse(exchange, 200, contentType, Files.readAllBytes(file));
    }

    private static void sendResponse(
            HttpExchange exchange, int status, String contentType, byte[] body)
            throws IOException {
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.getResponseHeaders().set("X-Content-Type-Options", "nosniff");
        exchange.sendResponseHeaders(status, body.length);

        try (var output = exchange.getResponseBody()) {
            output.write(body);
        }
    }
}
