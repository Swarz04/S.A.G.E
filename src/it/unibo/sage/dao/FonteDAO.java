package it.unibo.sage.dao;

import it.unibo.sage.model.Fonte;
import java.sql.SQLException;
import java.util.List;

public interface FonteDAO {

    List<Fonte> findDisponibiliPerUtente(String email) throws SQLException;
}
