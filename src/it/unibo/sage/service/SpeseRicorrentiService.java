package it.unibo.sage.service;

import it.unibo.sage.model.SpesaRicorrente;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SpeseRicorrentiService {

    public List<LocalDate> calcolaScadenzeDaGenerare(final SpesaRicorrente modello,
            final LocalDate finoA) {
        final List<LocalDate> scadenze = new ArrayList<>();
        LocalDate corrente = modello.getDataProssimaScadenza();

        while (!corrente.isAfter(finoA)
                && (modello.getScadenza() == null || !corrente.isAfter(modello.getScadenza()))) {
            scadenze.add(corrente);
            corrente = corrente.plusDays(modello.getFrequenzaGiorni());
        }

        return scadenze;
    }
}
