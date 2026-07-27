# ysoserial lab

Laboratorio Docker intenzionalmente vulnerabile per lo studio della
deserializzazione Java non sicura. Usare esclusivamente in un ambiente locale
e controllato.

La vittima `yoserial_lab` contiene:

- Commons Collections 4.0, per `CommonsCollections4`;
- Commons FileUpload 1.3.1 e Commons IO 2.4, per `FileUpload1`.

## Avvio

```sh
docker compose up -d --build
```

Applicazione:

```text
http://localhost:8084/
```

Container:

```text
yoserial_lab
```

## Arresto e ripristino

```sh
docker compose down
docker compose up -d --build --force-recreate
```

La ricreazione della vittima ripristina `/opt/flag_fantoccio.txt` e svuota
`/var/www/static`.

## File principali

- `Dockerfile`: costruisce la vittima unificata;
- `docker-compose.yml`: configura la vittima e la porta HTTP;
- `site/pom.xml`: dichiara le dipendenze vulnerabili;
- `site/src`: contiene l'applicazione Java.

Il repository contiene soltanto la vittima. Gli strumenti di attacco devono
essere mantenuti separatamente.

`site/flag.txt` deve contenere soltanto una flag dimostrativa prima della
pubblicazione su GitHub.
