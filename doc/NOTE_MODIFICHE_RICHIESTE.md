# Modifiche richieste

## 1. Spese ricorrenti collegate alle transazioni

Ogni scadenza maturata crea una normale riga in `TRANSIZIONE`. La colonna nullable
`ID_Ricorrenza` identifica il modello `SPESA_RICORRENTE` che ha generato la spesa.
La descrizione della transazione è il nome assegnato alla ricorrenza, ad esempio
`Netflix`, `Tinder` o `Abbonamento autobus`.

La generazione avviene automaticamente quando viene aperta la dashboard e resta
anche disponibile dal pulsante **Genera scadute**. L'indice univoco su
`(ID_Ricorrenza, Data)` impedisce la doppia generazione della stessa scadenza.

## 2. Icone di categorie e tag

Le tabelle `CATEGORIA` e `TAG` hanno la colonna `Icona`. Nei dialoghi di creazione
e modifica l'utente sceglie una delle icone incluse nel progetto. Il nome del file
viene validato dal servizio prima del salvataggio.

## 3. Rimozione di Essenziale e duplicati

Il tag `Essenziale` è stato rimosso dal popolamento e dalla configurazione iniziale.
La migrazione elimina sia l'eventuale versione di sistema sia quelle personali.
L'applicazione e i trigger SQL bloccano nuovi duplicati, ignorando differenze tra
maiuscole/minuscole e spazi iniziali/finali.

## 4. Aggiornamento del database

Per un database già importato, eseguire:

```sql
SOURCE doc/sql/aggiornamento_funzioni_richieste.sql;
```

In phpMyAdmin si può invece aprire il file e importarlo dalla scheda **Importa**.
Per un database nuovo usare nell'ordine:

1. `doc/sql/schema_completo.sql`
2. `doc/sql/trigger_viste.sql`
3. `doc/sql/popolamento.sql`
