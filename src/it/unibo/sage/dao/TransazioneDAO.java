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
}
