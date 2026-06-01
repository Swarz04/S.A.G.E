package it.unibo.sage.dao;

import it.unibo.sage.model.Documento;
import it.unibo.sage.model.Transazione;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public interface TransazioneDAO {

    long inserisci(Transazione transazione) throws SQLException;

    void associaTag(long idTransazione, long idTag) throws SQLException;

    void inserisciDocumento(Documento documento) throws SQLException;

    List<Transazione> findByPeriodo(String email, LocalDate dal, LocalDate al) throws SQLException;

    List<Transazione> findByCategoria(String email, long idCategoria) throws SQLException;

    List<Transazione> findByTag(String email, long idTag) throws SQLException;

    List<Transazione> findByFonte(String email, long idFonte) throws SQLException;

    Transazione findById(long idTransazione, String email) throws SQLException;

    List<Long> findTagIds(long idTransazione, String email) throws SQLException;

    void aggiorna(Transazione transazione) throws SQLException;

    void eliminaTag(long idTransazione) throws SQLException;

    void elimina(long idTransazione, String email) throws SQLException;
}
