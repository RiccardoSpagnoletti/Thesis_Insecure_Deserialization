# PHPGGC Monolog 3 lab

Laboratorio Docker intenzionalmente vulnerabile per lo studio della
deserializzazione PHP e delle gadget chain PHPGGC. Usare esclusivamente in un
ambiente locale e controllato.

La vittima `phpggc_lab` include PHP 8.1 e Monolog 3.0.0.

## Avvio

```sh
docker compose up -d --build
```

Applicazione:

```text
http://localhost:8400/
```

Container:

```text
phpggc_lab
```

## Verifica della dipendenza

```sh
docker exec phpggc_lab php -r \
  "require '/var/www/html/vendor/autoload.php'; echo Composer\\InstalledVersions::getPrettyVersion('monolog/monolog'), PHP_EOL;"
```

Il risultato atteso e' `3.0.0`.

## Arresto e ripristino

```sh
docker compose down
docker compose up -d --build --force-recreate
```

## File principali

- `Dockerfile`: costruisce la vittima con Monolog 3.0.0 gia' installato;
- `docker-compose.yml`: configura la vittima e la porta HTTP;
- `site/composer.json` e `site/composer.lock`: fissano le dipendenze;
- `site`: contiene l'applicazione PHP vulnerabile.

Il repository contiene soltanto la vittima. Gli strumenti di attacco devono
essere mantenuti separatamente.

Prima della pubblicazione su GitHub, i file sotto `site/private` devono
contenere solamente dati e flag dimostrativi.
