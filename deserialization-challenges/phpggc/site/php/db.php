<?php
require_once __DIR__ . '/../vendor/autoload.php';

function database_path(): string
{
    return __DIR__ . '/../data/paintings.sqlite';
}

function database(): PDO
{
    static $pdo = null;

    if ($pdo instanceof PDO) {
        return $pdo;
    }

    $path = database_path();
    $firstRun = !file_exists($path);
    $pdo = new PDO('sqlite:' . $path);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    $pdo->setAttribute(PDO::ATTR_DEFAULT_FETCH_MODE, PDO::FETCH_ASSOC);

    if ($firstRun) {
        initialize_database($pdo);
    }

    return $pdo;
}

function initialize_database(PDO $pdo): void
{
    $schema = file_get_contents(__DIR__ . '/../data/schema.sql');
    if ($schema === false) {
        throw new RuntimeException('Schema SQL non trovato.');
    }

    $pdo->exec($schema);
}

function load_paintings(): array
{
    $stmt = database()->query('SELECT id, titolo, autore, iban, prezzo, stato, prezzo_vendita FROM paintings ORDER BY id');
    return $stmt->fetchAll();
}

function add_painting(array $painting): void
{
    $stmt = database()->prepare(
        'INSERT INTO paintings (id, titolo, autore, iban, prezzo, stato, prezzo_vendita)
         VALUES (:id, :titolo, :autore, :iban, :prezzo, :stato, :prezzo_vendita)'
    );

    $stmt->execute([
        ':id' => (string) $painting['id'],
        ':titolo' => (string) $painting['titolo'],
        ':autore' => (string) $painting['autore'],
        ':iban' => (string) $painting['iban'],
        ':prezzo' => (float) $painting['prezzo'],
        ':stato' => (string) $painting['stato'],
        ':prezzo_vendita' => $painting['prezzo_vendita'] === null ? null : (float) $painting['prezzo_vendita'],
    ]);
}

function validate_painting_record($record): array
{
    if (!is_array($record)) {
        throw new InvalidArgumentException('Record non valido.');
    }

    $required = ['id', 'titolo', 'autore', 'iban', 'prezzo', 'stato', 'prezzo_vendita'];
    foreach ($required as $field) {
        if (!array_key_exists($field, $record)) {
            throw new InvalidArgumentException('Campo mancante: ' . $field);
        }
    }

    $record['id'] = trim((string) $record['id']);
    $record['titolo'] = trim((string) $record['titolo']);
    $record['autore'] = trim((string) $record['autore']);
    $record['iban'] = trim((string) $record['iban']);
    $record['stato'] = trim((string) $record['stato']);

    if ($record['id'] === '' || $record['titolo'] === '' || $record['autore'] === '' || $record['iban'] === '') {
        throw new InvalidArgumentException('I campi testuali non possono essere vuoti.');
    }

    if (!is_numeric($record['prezzo']) || (float) $record['prezzo'] < 0) {
        throw new InvalidArgumentException('Prezzo non valido.');
    }

    if (!in_array($record['stato'], ['in_asta', 'venduto', 'magazzino'], true)) {
        throw new InvalidArgumentException('Stato non valido.');
    }

    if ($record['prezzo_vendita'] !== null && $record['prezzo_vendita'] !== '') {
        if (!is_numeric($record['prezzo_vendita']) || (float) $record['prezzo_vendita'] < 0) {
            throw new InvalidArgumentException('Prezzo di vendita non valido.');
        }
        $record['prezzo_vendita'] = (float) $record['prezzo_vendita'];
    } else {
        $record['prezzo_vendita'] = null;
    }

    $record['prezzo'] = (float) $record['prezzo'];

    return $record;
}

function format_price(float $value): string
{
    return number_format($value, 0, ',', '.') . ' EUR';
}

function format_status(string $status): string
{
    if ($status === 'in_asta') {
        return 'In asta';
    }
    if ($status === 'venduto') {
        return 'Venduto';
    }
    return 'Magazzino';
}

function status_class(string $status): string
{
    if ($status === 'in_asta') {
        return 'in-asta';
    }
    if ($status === 'venduto') {
        return 'venduto';
    }
    return 'magazzino';
}
