package it.unibo.sage.controller;

import it.unibo.sage.model.Categoria;
import it.unibo.sage.model.Fonte;
import it.unibo.sage.model.Tag;
import it.unibo.sage.service.MovimentiService;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class MovimentiController {

    private final MovimentiService movimentiService = new MovimentiService();

    public List<Categoria> caricaCategorieDisponibili(final String email) throws SQLException {
        return movimentiService.caricaCategorieDisponibili(email);
    }

    public List<Tag> caricaTagDisponibili(final String email) throws SQLException {
        return movimentiService.caricaTagDisponibili(email);
    }

    public List<Fonte> caricaFontiDisponibili(final String email) throws SQLException {
        return movimentiService.caricaFontiDisponibili(email);
    }

    public List<it.unibo.sage.model.Transazione> caricaTransazioni(final String email,
            final LocalDate dal, final LocalDate al) throws SQLException {
        return movimentiService.caricaTransazioni(email, dal, al);
    }

    public long registraSpesa(final String email, final BigDecimal importo,
            final LocalDate data, final String descrizione, final long idCategoria,
            final List<Long> idTag, final String documentoPath) throws SQLException {
        return movimentiService.registraSpesa(email, importo, data, descrizione, idCategoria,
                idTag, documentoPath);
    }

    public long registraEntrata(final String email, final BigDecimal importo,
            final LocalDate data, final String descrizione, final long idFonte) throws SQLException {
        return movimentiService.registraEntrata(email, importo, data, descrizione, idFonte);
    }
}
