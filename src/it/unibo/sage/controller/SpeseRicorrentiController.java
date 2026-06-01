package it.unibo.sage.controller;

import it.unibo.sage.model.SpesaRicorrente;
import it.unibo.sage.service.SpeseRicorrentiService;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class SpeseRicorrentiController {

    private final SpeseRicorrentiService speseRicorrentiService = new SpeseRicorrentiService();

    public List<SpesaRicorrente> caricaRicorrenze(final String email) throws SQLException {
        return speseRicorrentiService.caricaRicorrenze(email);
    }

    public long aggiungiRicorrenza(final String email, final BigDecimal importo,
            final int frequenzaGiorni, final LocalDate dataInizio,
            final LocalDate dataProssimaScadenza, final LocalDate scadenza,
            final long idCategoria) throws SQLException {
        return speseRicorrentiService.aggiungiRicorrenza(email, importo, frequenzaGiorni,
                dataInizio, dataProssimaScadenza, scadenza, idCategoria);
    }

    public void eliminaRicorrenza(final String email, final long idRicorrenza)
            throws SQLException {
        speseRicorrentiService.eliminaRicorrenza(email, idRicorrenza);
    }

    public int generaSpeseScadute(final String email, final LocalDate finoA) throws SQLException {
        return speseRicorrentiService.generaSpeseScadute(email, finoA);
    }

    public List<LocalDate> calcolaScadenzeDaGenerare(final SpesaRicorrente modello,
            final LocalDate finoA) {
        return speseRicorrentiService.calcolaScadenzeDaGenerare(modello, finoA);
    }
}
