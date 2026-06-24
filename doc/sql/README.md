# Script SQL consolidati S.A.G.E.

La cartella `doc/sql` contiene quattro script SQL consolidati:

1. `01_schema_completo.sql` crea da zero schema, tabelle, vincoli, trigger e viste.
2. `02_popolamento_demo.sql` inserisce i dati dimostrativi estesi: due account studente, account admin tecnico, categorie/fonti/tag e transazioni da gennaio a giugno 2026 con almeno 7/8 movimenti per mese e budget unici per utente/ambito.
3. `03_migrazione_database_esistente.sql` aggiorna un database gia' esistente senza ricrearlo da zero.
4. `04_query_operazioni.sql` raccoglie query operative e di consultazione.

Per una nuova installazione eseguire nell'ordine `01_schema_completo.sql` e poi `02_popolamento_demo.sql`.
Per aggiornare una installazione gia' popolata usare invece `03_migrazione_database_esistente.sql`, evitando di rieseguire lo script di creazione completa se si vogliono conservare i dati.


Nota pratica: copiare o aggiornare i file Java non modifica automaticamente i dati gia' presenti in MySQL. Se nella schermata Transazioni compaiono ancora pochi movimenti demo, significa che il database locale non e' stato ricaricato con il nuovo popolamento. Per vedere il dataset esteso bisogna eseguire nuovamente `01_schema_completo.sql` e poi `02_popolamento_demo.sql` su un database di prova, sapendo che il primo script ricrea lo schema da zero.

Budget: il vincolo `UQ_BUDGET_AMBITO` ammette un solo budget globale per utente e un solo budget per ciascuna categoria. Il limite viene riusato mese per mese; l'applicazione aggiorna il periodo e il totale speso al mese corrente.
