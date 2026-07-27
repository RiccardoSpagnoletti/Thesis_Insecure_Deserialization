import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ChallengeServer {
    private static final String STATIC_PATH = "/var/www/static";

    private static final List<Painting> PAINTINGS = new ArrayList<Painting>(Arrays.asList(
            new Painting("Q001", "Luce sul porto", "L. Bianchi", "IT00X0000000000000000000000", 1200, "venduto", 9999.0),
            new Painting("Q002", "Campo di primavera", "M. Rossi", "IT89W0101000320000001234567", 900, "magazzino", null),
            new Painting("Q003", "Notte marina", "G. Verdi", "IT12A0306909606100000123456", 1500, "in_asta", null),
            new Painting("Q004", "Ritratto in luce", "F. Neri", "IT45K0200804510000123456789", 2000, "venduto", 2600.0)
    ));


    private ChallengeServer() {
    }

    // Avvia il server HTTP e registra le rotte disponibili.
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", 8080), 0);
        server.createContext("/", ChallengeServer::home);
        server.createContext("/css/style.css", ChallengeServer::style);
        server.createContext("/static", new StaticFileHandler(STATIC_PATH));
        server.createContext("/java/aggiungi_quadro", ChallengeServer::addPainting);
        server.setExecutor(null);
        server.start();

        System.out.println("ysoserial challenge listening on :8080");
    }

    // Gestione della pagina principale  catalogo + form.
    private static void home(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            respond(exchange, 405, "Method not allowed\n", "text/plain");
            return;
        }
        if (!"/".equals(exchange.getRequestURI().getPath())) {
            respond(exchange, 404, "Not found\n", "text/plain");
            return;
        }

        String addStatus = queryParams(exchange).get("add");
        String html = Painting.renderHome(PAINTINGS, addStatus, serializeToBase64(new PaintingEntry()));

        respond(exchange, 200, html, "text/html; charset=utf-8");
    }

    // Legge il CSS incluso tra le risorse del JAR.
    private static void style(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            respond(exchange, 405, "Method not allowed\n", "text/plain");
            return;
        }

        String css = readResource("/style.css");
        respond(exchange, 200, css, "text/css; charset=utf-8");
    }

    // Ricevo il form e deserializzo il painting_entry.
    private static void addPainting(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            redirect(exchange, "/?add=missing");
            return;
        }

        Map<String, String> params = formParams(exchange);
        String serializedEntry = params.get("painting_entry");
        if (serializedEntry == null || serializedEntry.trim().isEmpty()) {
            redirect(exchange, "/?add=missing");
            return;
        }

        try {
            byte[] serialized = Base64.getDecoder().decode(serializedEntry);
            Object object = deserialize(serialized);
            if (!(object instanceof PaintingEntry)) {
                redirect(exchange, "/?add=invalid_token");
                return;
            }

            PAINTINGS.add(new Painting(
                    requiredParam(params, "id"),
                    requiredParam(params, "titolo"),
                    requiredParam(params, "autore"),
                    requiredParam(params, "iban"),
                    parseNumber(params.get("prezzo")),
                    normalizeStatus(params.get("stato")),
                    parseNullableNumber(params.get("prezzo_vendita"))));
            redirect(exchange, "/?add=ok");
        } catch (Throwable throwable) {
            redirect(exchange, "/?add=unserialize_error");
        }
    }

    // Deserializzo un array di byte in un oggetto Java.
    private static Object deserialize(byte[] serialized) throws IOException, ClassNotFoundException {
        try (ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(serialized))) {
            return input.readObject();
        }
    }

    // Serializzo un oggetto Java e lo porto in Base64.
    private static String serializeToBase64(Serializable object) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (ObjectOutputStream objectOutput = new ObjectOutputStream(output)) {
            objectOutput.writeObject(object);
        }
        return Base64.getEncoder().encodeToString(output.toByteArray());
    }

    // Leggo un parametro obbligatorio dal form.
    private static String requiredParam(Map<String, String> params, String name) {
        String value = params.get(name);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Missing parameter: " + name);
        }
        return value.trim();
    }

    // Converto un valore testuale in numero.
    private static double parseNumber(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Missing numeric value");
        }
        return Double.parseDouble(value.trim());
    }

    // Converte un valore testuale opzionale in numero.
    private static Double parseNullableNumber(String value) {
        return value == null || value.trim().isEmpty() ? null : Double.valueOf(value);
    }

    // Valido lo stato del quadro.
    private static String normalizeStatus(String value) {
        if ("in_asta".equals(value) || "venduto".equals(value) || "magazzino".equals(value)) {
            return value;
        }
        throw new IllegalArgumentException("Invalid status");
    }

    // Estrae e interpreta i parametri del body form-urlencoded.
    private static Map<String, String> formParams(HttpExchange exchange) throws IOException {
        String body = new String(readAll(exchange.getRequestBody()), StandardCharsets.UTF_8);
        return parseParams(body);
    }

    // Estrae e interpreta i parametri della query string.
    private static Map<String, String> queryParams(HttpExchange exchange) {
        String query = exchange.getRequestURI().getRawQuery();
        return parseParams(query == null ? "" : query);
    }

    // Converte una stringa form-urlencoded in una mappa di parametri.
    private static Map<String, String> parseParams(String body) {
        Map<String, String> params = new LinkedHashMap<String, String>();
        if (body.isEmpty()) {
            return params;
        }

        for (String pair : body.split("&")) {
            int separator = pair.indexOf('=');
            String key = separator >= 0 ? pair.substring(0, separator) : pair;
            String value = separator >= 0 ? pair.substring(separator + 1) : "";
            params.put(urlDecode(key), urlDecode(value));
        }
        return params;
    }

    // Decodifica un valore URL-encoded.
    private static String urlDecode(String value) {
        try {
            return URLDecoder.decode(value, "UTF-8");
        } catch (Exception e) {
            return "";
        }
    }

    // Legge completamente uno stream in memoria.
    private static byte[] readAll(InputStream inputStream) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int read;
        while ((read = inputStream.read(buffer)) != -1) {
            output.write(buffer, 0, read);
        }
        return output.toByteArray();
    }

    // Legge una risorsa inclusa nel classpath.
    private static String readResource(String resourceName) throws IOException {
        InputStream input = ChallengeServer.class.getResourceAsStream(resourceName);
        if (input == null) {
            throw new IOException("Missing resource: " + resourceName);
        }
        try {
            return new String(readAll(input), StandardCharsets.UTF_8);
        } finally {
            input.close();
        }
    }

    // Invia una risposta HTTP di redirect.
    private static void redirect(HttpExchange exchange, String location) throws IOException {
        exchange.getResponseHeaders().set("Location", location);
        exchange.sendResponseHeaders(302, -1);
        exchange.close();
    }

    // Invia una risposta HTTP con corpo e Content-Type.
    private static void respond(HttpExchange exchange, int status, String body, String contentType) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        Headers headers = exchange.getResponseHeaders();
        headers.set("Content-Type", contentType);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }

    public static final class PaintingEntry implements Serializable {
        private static final long serialVersionUID = 1L;
    }

    private static final class StaticFileHandler implements HttpHandler {
        private final Path root;

        StaticFileHandler(String rootDirectory) throws IOException {
            Files.createDirectories(Paths.get(rootDirectory));
            this.root = Paths.get(rootDirectory).toRealPath();
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equals(exchange.getRequestMethod())) {
                respond(exchange, 405, "Method not allowed\n", "text/plain");
                return;
            }

            String requestPath = exchange.getRequestURI().getPath();
            String relativePath = requestPath.substring("/static".length());
            Path requested = relativePath.isEmpty() || "/".equals(relativePath)
                    ? root
                    : root.resolve(relativePath.substring(1)).normalize();
            if (!requested.startsWith(root)) {
                respond(exchange, 404, "Not found\n", "text/plain");
                return;
            }
            if (Files.isDirectory(requested)) {
                respond(exchange, 200, listDirectory(requested), "text/plain; charset=utf-8");
                return;
            }
            if (!Files.isRegularFile(requested)) {
                respond(exchange, 404, "Not found\n", "text/plain");
                return;
            }

            String body = new String(Files.readAllBytes(requested), StandardCharsets.UTF_8);
            respond(exchange, 200, body, "text/plain; charset=utf-8");
        }

        private String listDirectory(Path directory) throws IOException {
            StringBuilder listing = new StringBuilder();
            try (DirectoryStream<Path> entries = Files.newDirectoryStream(directory)) {
                for (Path entry : entries) {
                    listing.append(entry.getFileName());
                    if (Files.isDirectory(entry)) {
                        listing.append('/');
                    }
                    listing.append('\n');
                }
            }
            return listing.toString();
        }
    }
}
