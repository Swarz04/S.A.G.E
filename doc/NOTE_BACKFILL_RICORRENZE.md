# Correzione storico spese ricorrenti

La generazione delle ricorrenze ora parte da `Data_Inizio` e registra tutte le
scadenze maturate fino alla data corrente nella tabella `TRANSIZIONE`.

- Le transazioni sono collegate tramite `ID_Ricorrenza`.
- Le date già presenti non vengono duplicate.
- Le ricorrenze già salvate ma con mesi mancanti vengono riparate all'avvio o
  premendo **Genera scadute**.
- Nell'interfaccia, frequenza `30` indica una ricorrenza mensile e mantiene il
  giorno del mese; frequenza `365` indica una ricorrenza annuale.
- `Data_Prossima_Scadenza` viene riallineata alla prima data successiva a oggi.

Non sono necessarie modifiche allo schema XAMPP/MySQL: vengono usati i campi
`TRANSIZIONE.ID_Ricorrenza` e l'indice univoco già previsti dagli script SQL del
progetto.
