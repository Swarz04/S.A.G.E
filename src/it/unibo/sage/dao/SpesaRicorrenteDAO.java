package it.unibo.sage.dao;

import it.unibo.sage.model.SpesaRicorrente;
import java.time.LocalDate;
import java.util.List;
import java.sql.SQLException;

public interface SpesaRicorrenteDAO {

    List<SpesaRicorrente> findByEmail(String email) throws SQLException;

    List<SpesaRicorrente> findScadute(String email, LocalDate finoA) throws SQLException;

    long inserisci(SpesaRicorrente spesaRicorrente) throws SQLException;

    void aggiornaProssimaScadenza(long idRicorrenza, String email, LocalDate nuovaScadenza)
            throws SQLException;

    void aggiorna(SpesaRicorrente spesaRicorrente) throws SQLException;

    void elimina(long idRicorrenza, String email) throws SQLException;
}
