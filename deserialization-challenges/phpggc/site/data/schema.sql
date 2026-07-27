CREATE TABLE IF NOT EXISTS paintings (
    id TEXT PRIMARY KEY,
    titolo TEXT NOT NULL,
    autore TEXT NOT NULL,
    iban TEXT NOT NULL,
    prezzo REAL NOT NULL,
    stato TEXT NOT NULL CHECK (stato IN ('in_asta', 'venduto', 'magazzino')),
    prezzo_vendita REAL NULL
);

INSERT OR IGNORE INTO paintings (id, titolo, autore, iban, prezzo, stato, prezzo_vendita) VALUES
('Q001', 'Luce sul porto', 'L. Bianchi', 'IT00X0000000000000000000000', 1200, 'venduto', 9999),
('Q002', 'Campo di primavera', 'M. Rossi', 'IT89W0101000320000001234567', 900, 'magazzino', NULL),
('Q003', 'Notte marina', 'G. Verdi', 'IT12A0306909606100000123456', 1500, 'in_asta', NULL),
('Q004', 'Ritratto in luce', 'F. Neri', 'IT45K0200804510000123456789', 2000, 'venduto', 2600);
