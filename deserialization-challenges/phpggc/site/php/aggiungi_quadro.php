<?php
// -------------------------------------------------------------------------
// LABORATORIO LOCALE INTENZIONALMENTE VULNERABILE
// -------------------------------------------------------------------------
// Questa pagina serve solo per una tesi/lab CTF su PHP Object Injection.
// Non usare mai unserialize() su input controllato dall'utente in produzione.
// Il server si aspetta un array PHP serializzato con dati di un quadro, ma un
// attaccante puo' inviare un oggetto serializzato Monolog generato da PHPGGC.

require __DIR__ . "/../vendor/autoload.php";
require __DIR__ . "/db.php";
require __DIR__ . "/logger.php";

function redirect_to_catalog(string $status): void
{
    header("Location: ../index.php?add=" . rawurlencode($status));
    exit;
}

if ($_SERVER["REQUEST_METHOD"] !== "POST" || !isset($_POST["painting_entry"])) {
    app_logger()->warning("Richiesta di inserimento quadro non valida");
    redirect_to_catalog("missing");
}

try {
    // ================================================================
    // RIGA VULNERABILE: deserializzazione di input utente non fidato.
    // ================================================================
    $result = unserialize($_POST["painting_entry"]);
} catch (Throwable $e) {
    app_logger()->error("Errore durante la deserializzazione del quadro", [
        "exception" => get_class($e),
        "message" => $e->getMessage(),
    ]);
    redirect_to_catalog("unserialize_error");
}

if (is_array($result)) {
    try {
        $painting = validate_painting_record($result);
        add_painting($painting);
        app_logger()->info("Quadro aggiunto al catalogo", [
            "id" => $painting["id"],
            "titolo" => $painting["titolo"],
            "autore" => $painting["autore"],
        ]);
        redirect_to_catalog("ok");
    } catch (Throwable $e) {
        app_logger()->warning("Dati quadro non validi", [
            "exception" => get_class($e),
            "message" => $e->getMessage(),
        ]);
        redirect_to_catalog("invalid");
    }
} else {
    app_logger()->warning("Payload deserializzato non compatibile con un quadro", [
        "tipo" => is_object($result) ? get_class($result) : gettype($result),
    ]);
    redirect_to_catalog("not_array");
}
