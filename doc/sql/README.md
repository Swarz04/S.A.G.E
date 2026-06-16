# Script SQL di S.A.G.E.

La cartella contiene soltanto quattro script:

1. `01_schema_completo.sql`: crea da zero database, tabelle, indici, trigger e viste. Elimina un eventuale database omonimo.
2. `02_popolamento_demo.sql`: inserisce utenti e dati dimostrativi. Va eseguito dopo lo schema completo.
3. `03_migrazione_database_esistente.sql`: aggiorna un database creato con una versione precedente senza ricrearlo. Sostituisce tutte le vecchie migrazioni separate.
4. `04_query_operazioni.sql`: raccolta facoltativa di query dimostrative e operative.

## Database nuovo

Eseguire nell'ordine `01_schema_completo.sql` e, se servono i dati demo, `02_popolamento_demo.sql`.

## Database gia esistente

Eseguire soltanto `03_migrazione_database_esistente.sql`. Prima e consigliato esportare un backup da phpMyAdmin.
