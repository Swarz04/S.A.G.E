# Ricorrenze collegate alle transazioni

Quando l’utente crea una nuova spesa ricorrente dall’interfaccia, il programma ora:

1. salva il modello in `SPESA_RICORRENTE`;
2. crea immediatamente una spesa in `TRANSIZIONE` con `ID_Ricorrenza`;
3. aggiorna i budget interessati;
4. sposta la prossima scadenza in avanti se coincide con la data odierna o è precedente.

La prima spesa usa la data corrente, quindi compare subito nello storico e nei grafici del mese.
Non è richiesta alcuna modifica SQL aggiuntiva: il collegamento `TRANSIZIONE.ID_Ricorrenza` è già previsto dagli script precedenti.
