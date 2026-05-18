package it.unibo.sage.controller;

import it.unibo.sage.model.SpesaRicorrente;
import it.unibo.sage.service.SpeseRicorrentiService;
import java.time.LocalDate;
import java.util.List;

public class SpeseRicorrentiController {

    private final SpeseRicorrentiService speseRicorrentiService = new SpeseRicorrentiService();

    public List<LocalDate> calcolaScadenzeDaGenerare(final SpesaRicorrente modello,
            final LocalDate finoA) {
        return speseRicorrentiService.calcolaScadenzeDaGenerare(modello, finoA);
    }
}
