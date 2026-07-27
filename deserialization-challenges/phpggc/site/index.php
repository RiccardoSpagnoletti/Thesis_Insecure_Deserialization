<?php
require __DIR__ . '/php/db.php';

$paintings = load_paintings();
$addStatus = $_GET['add'] ?? null;
?>
<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Aste di Quadri</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
    <div class="app">
        <header class="header">
            <div>
                <p class="eyebrow">Capture the Flag lab</p>
                <h1>Aste di Quadri</h1>
                <p class="subtitle">Catalogo pubblico con inserimento quadri serializzato lato client.</p>
            </div>
        </header>

        <section class="panel">
            <div class="panel-header">
                <h2>Aggiungi quadro</h2>
            </div>
            <?php if ($addStatus === 'ok'): ?>
                <p class="notice success">Quadro aggiunto al catalogo.</p>
            <?php elseif ($addStatus !== null): ?>
                <p class="notice error">Il quadro non e' stato aggiunto. Controlla i dati inseriti.</p>
            <?php endif; ?>
            <form id="addPaintingForm" class="form-grid" method="post" action="php/aggiungi_quadro.php">
                <label>
                    ID
                    <input type="text" name="id" value="Q005" required>
                </label>
                <label>
                    Titolo
                    <input type="text" name="titolo" value="Studio sul mare" required>
                </label>
                <label>
                    Autore
                    <input type="text" name="autore" value="L. Ferri" required>
                </label>
                <label>
                    IBAN proprietario
                    <input type="text" name="iban" value="IT60X0542811101000000123456" required>
                </label>
                <label>
                    Prezzo di partenza
                    <input type="number" name="prezzo" min="0" step="1" value="1200" required>
                </label>
                <label>
                    Stato
                    <select name="stato">
                        <option value="in_asta">In asta</option>
                        <option value="venduto">Venduto</option>
                        <option value="magazzino">Magazzino</option>
                    </select>
                </label>
                <label>
                    Prezzo di vendita
                    <input type="number" name="prezzo_vendita" min="0" step="1">
                </label>
                <input id="serializedPainting" type="hidden" name="painting_entry">
                <button class="btn" type="submit">Aggiungi quadro</button>
            </form>
        </section>

        <section class="panel">
            <h2>Catalogo quadri</h2>
            <?php if (count($paintings) === 0): ?>
                <p>Nessun quadro presente.</p>
            <?php else: ?>
                <div class="grid">
                    <?php foreach ($paintings as $q): ?>
                        <article class="card">
                            <div class="card-header">
                                <h3><?php echo htmlspecialchars($q['titolo']); ?></h3>
                                <span class="status <?php echo status_class($q['stato']); ?>"><?php echo format_status($q['stato']); ?></span>
                            </div>
                            <p class="meta">ID: <?php echo htmlspecialchars($q['id']); ?></p>
                            <p class="meta">Autore: <?php echo htmlspecialchars($q['autore']); ?></p>
                            <p class="meta">IBAN proprietario: <?php echo htmlspecialchars($q['iban']); ?></p>
                            <p class="price">Base: <?php echo format_price((float) $q['prezzo']); ?></p>
                            <?php if ($q['prezzo_vendita'] !== null): ?>
                                <p class="meta">Vendita: <?php echo format_price((float) $q['prezzo_vendita']); ?></p>
                            <?php endif; ?>
                        </article>
                    <?php endforeach; ?>
                </div>
            <?php endif; ?>
        </section>
    </div>

    <script>
    const form = document.getElementById("addPaintingForm");
    const serializedPainting = document.getElementById("serializedPainting");
    const encoder = new TextEncoder();

    function phpString(value) {
        return `s:${encoder.encode(value).length}:"${value}";`;
    }

    function phpNumber(value) {
        const number = Number(value || 0);
        return Number.isInteger(number) ? `i:${number};` : `d:${number};`;
    }

    function phpNullableNumber(value) {
        return value === "" ? "N;" : phpNumber(value);
    }

    function buildSerializedPainting() {
        const fields = [
            ["id", phpString(form.elements.id.value)],
            ["titolo", phpString(form.elements.titolo.value)],
            ["autore", phpString(form.elements.autore.value)],
            ["iban", phpString(form.elements.iban.value)],
            ["prezzo", phpNumber(form.elements.prezzo.value)],
            ["stato", phpString(form.elements.stato.value)],
            ["prezzo_vendita", phpNullableNumber(form.elements.prezzo_vendita.value)]
        ];

        return `a:${fields.length}:{` + fields
            .map(([key, value]) => phpString(key) + value)
            .join("") + "}";
    }

    function refreshSerializedPainting() {
        const serializedPaintingValue = buildSerializedPainting();
        serializedPainting.value = serializedPaintingValue;
    }

    form.addEventListener("input", refreshSerializedPainting);
    form.addEventListener("submit", refreshSerializedPainting);
    refreshSerializedPainting();
    </script>
</body>
</html>
