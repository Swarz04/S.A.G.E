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

    public void aggiungiCategoriaPersonale(final String email, final String nome) throws SQLException {
        movimentiService.aggiungiCategoriaPersonale(email, nome);
    }

    public void aggiungiCategoriaPersonale(final String email, final String nome,
            final String icona) throws SQLException {
        movimentiService.aggiungiCategoriaPersonale(email, nome, icona);
    }

    public void aggiungiTagPersonale(final String email, final String nome) throws SQLException {
        movimentiService.aggiungiTagPersonale(email, nome);
    }

    public void aggiungiTagPersonale(final String email, final String nome,
            final String icona) throws SQLException {
        movimentiService.aggiungiTagPersonale(email, nome, icona);
    }

    public void aggiungiFontePersonale(final String email, final String nome) throws SQLException {
        movimentiService.aggiungiFontePersonale(email, nome);
    }

    public void aggiungiFontePersonale(final String email, final String nome,
            final String icona) throws SQLException {
        movimentiService.aggiungiFontePersonale(email, nome, icona);
    }

    public void rinominaCategoriaPersonale(final String email, final long idCategoria,
            final String nome) throws SQLException {
        movimentiService.rinominaCategoriaPersonale(email, idCategoria, nome);
    }

    public void modificaCategoriaPersonale(final String email, final long idCategoria,
            final String nome, final String icona) throws SQLException {
        movimentiService.modificaCategoriaPersonale(email, idCategoria, nome, icona);
    }

    public void eliminaCategoriaPersonale(final String email, final long idCategoria)
            throws SQLException {
        movimentiService.eliminaCategoriaPersonale(email, idCategoria);
    }

    public void rinominaTagPersonale(final String email, final long idTag,
            final String nome) throws SQLException {
        movimentiService.rinominaTagPersonale(email, idTag, nome);
    }

    public void modificaTagPersonale(final String email, final long idTag,
            final String nome, final String icona) throws SQLException {
        movimentiService.modificaTagPersonale(email, idTag, nome, icona);
    }

    public void eliminaTagPersonale(final String email, final long idTag) throws SQLException {
        movimentiService.eliminaTagPersonale(email, idTag);
    }

    public void rinominaFontePersonale(final String email, final long idFonte,
            final String nome) throws SQLException {
        movimentiService.rinominaFontePersonale(email, idFonte, nome);
    }

    public void modificaFontePersonale(final String email, final long idFonte,
            final String nome, final String icona) throws SQLException {
        movimentiService.modificaFontePersonale(email, idFonte, nome, icona);
    }

    public void eliminaFontePersonale(final String email, final long idFonte) throws SQLException {
        movimentiService.eliminaFontePersonale(email, idFonte);
    }

    public List<it.unibo.sage.model.Transazione> caricaTransazioni(final String email,
            final LocalDate dal, final LocalDate al) throws SQLException {
        return movimentiService.caricaTransazioni(email, dal, al);
    }

    public List<it.unibo.sage.model.Transazione> caricaTransazioniPerCategoria(
            final String email, final long idCategoria) throws SQLException {
        return movimentiService.caricaTransazioniPerCategoria(email, idCategoria);
    }

    public List<it.unibo.sage.model.Transazione> caricaTransazioniPerTag(
            final String email, final long idTag) throws SQLException {
        return movimentiService.caricaTransazioniPerTag(email, idTag);
    }

    public List<it.unibo.sage.model.Transazione> caricaTransazioniPerFonte(
            final String email, final long idFonte) throws SQLException {
        return movimentiService.caricaTransazioniPerFonte(email, idFonte);
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

    public List<Long> caricaTagTransazione(final String email, final long idTransazione)
            throws SQLException {
        return movimentiService.caricaTagTransazione(email, idTransazione);
    }

    public void aggiornaTransazione(final String email, final long idTransazione,
            final BigDecimal importo, final LocalDate data, final String descrizione,
            final Long idCategoria, final Long idFonte, final List<Long> idTag)
            throws SQLException {
        movimentiService.aggiornaTransazione(email, idTransazione, importo, data,
                descrizione, idCategoria, idFonte, idTag);
    }

    public void eliminaTransazione(final String email, final long idTransazione)
            throws SQLException {
        movimentiService.eliminaTransazione(email, idTransazione);
    }
}
