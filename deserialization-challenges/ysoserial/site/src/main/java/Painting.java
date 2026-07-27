import java.util.List;

final class Painting {
    private final String id;
    private final String title;
    private final String author;
    private final String iban;
    private final double price;
    private final String status;
    private final Double salePrice;

    // Inizializza i dati di un quadro.
    Painting(String id, String title, String author, String iban, double price, String status, Double salePrice) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.iban = iban;
        this.price = price;
        this.status = status;
        this.salePrice = salePrice;
    }

    // Genera l'HTML completo della pagina principale.
    static String renderHome(List<Painting> paintings, String addStatus, String serializedEntry) {
        return "<!DOCTYPE html><html lang=\"it\"><head>"
                + "<meta charset=\"UTF-8\">"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"
                + "<title>Aste di Quadri</title>"
                + "<link rel=\"stylesheet\" href=\"css/style.css\">"
                + "</head><body><div class=\"app\">"
                + "<header class=\"header\"><div>"
                + "<p class=\"eyebrow\">Capture the Flag lab</p>"
                + "<h1>Aste di Quadri</h1>"
                + "<p class=\"subtitle\">Catalogo pubblico con token Java serializzato restituito dal client.</p>"
                + "</div></header>"
                + "<section class=\"panel\"><div class=\"panel-header\"><h2>Aggiungi quadro</h2></div>"
                + statusNotice(addStatus)
                + "<form class=\"form-grid\" method=\"post\" action=\"/java/aggiungi_quadro\">"
                + "<label>ID<input type=\"text\" name=\"id\" value=\"Q005\" required></label>"
                + "<label>Titolo<input type=\"text\" name=\"titolo\" value=\"Studio sul mare\" required></label>"
                + "<label>Autore<input type=\"text\" name=\"autore\" value=\"L. Ferri\" required></label>"
                + "<label>IBAN proprietario<input type=\"text\" name=\"iban\" value=\"IT60X0542811101000000123456\" required></label>"
                + "<label>Prezzo di partenza<input type=\"number\" name=\"prezzo\" min=\"0\" step=\"1\" value=\"1200\" required></label>"
                + "<label>Stato<select name=\"stato\">"
                + "<option value=\"in_asta\">In asta</option>"
                + "<option value=\"venduto\">Venduto</option>"
                + "<option value=\"magazzino\">Magazzino</option>"
                + "</select></label>"
                + "<label>Prezzo di vendita<input type=\"number\" name=\"prezzo_vendita\" min=\"0\" step=\"1\"></label>"
                + "<input type=\"hidden\" name=\"painting_entry\" value=\"" + serializedEntry + "\">"
                + "<button class=\"btn\" type=\"submit\">Aggiungi quadro</button>"
                + "</form></section>"
                + "<section class=\"panel\"><h2>Catalogo quadri</h2><div class=\"grid\">"
                + renderPaintingCards(paintings)
                + "</div></section>"
                + "</div></body></html>";
    }

    // Genera le card HTML dei quadri in catalogo.
    private static String renderPaintingCards(List<Painting> paintings) {
        StringBuilder cards = new StringBuilder();
        for (Painting painting : paintings) {
            cards.append("<article class=\"card\">")
                    .append("<div class=\"card-header\">")
                    .append("<h3>").append(escape(painting.title)).append("</h3>")
                    .append("<span class=\"status ").append(statusClass(painting.status)).append("\">")
                    .append(formatStatus(painting.status)).append("</span>")
                    .append("</div>")
                    .append("<p class=\"meta\">ID: ").append(escape(painting.id)).append("</p>")
                    .append("<p class=\"meta\">Autore: ").append(escape(painting.author)).append("</p>")
                    .append("<p class=\"meta\">IBAN proprietario: ").append(escape(painting.iban)).append("</p>")
                    .append("<p class=\"price\">Base: ").append(formatPrice(painting.price)).append("</p>");
            if (painting.salePrice != null) {
                cards.append("<p class=\"meta\">Vendita: ").append(formatPrice(painting.salePrice)).append("</p>");
            }
            cards.append("</article>");
        }
        return cards.toString();
    }

    // Genera il messaggio di esito dell'inserimento.
    private static String statusNotice(String addStatus) {
        if (addStatus == null) {
            return "";
        }
        if ("ok".equals(addStatus)) {
            return "<p class=\"notice success\">Quadro aggiunto al catalogo.</p>";
        }
        return "<p class=\"notice error\">Il quadro non e' stato aggiunto. Controlla i dati inseriti.</p>";
    }

    // Formatta un prezzo in euro senza decimali.
    private static String formatPrice(double value) {
        String number = String.format(java.util.Locale.US, "%.0f", value);
        StringBuilder result = new StringBuilder();
        int count = 0;
        for (int i = number.length() - 1; i >= 0; i--) {
            if (count > 0 && count % 3 == 0) {
                result.insert(0, '.');
            }
            result.insert(0, number.charAt(i));
            count++;
        }
        return result + " EUR";
    }

    // Converte lo stato interno in testo leggibile.
    private static String formatStatus(String status) {
        if ("in_asta".equals(status)) {
            return "In asta";
        }
        if ("venduto".equals(status)) {
            return "Venduto";
        }
        return "Magazzino";
    }

    // Converte lo stato interno in classe CSS.
    private static String statusClass(String status) {
        if ("in_asta".equals(status)) {
            return "in-asta";
        }
        if ("venduto".equals(status)) {
            return "venduto";
        }
        return "magazzino";
    }

    // Escapa i caratteri HTML per valori mostrati nella pagina.
    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
