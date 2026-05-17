package it.unibo.sage.dao;

import it.unibo.sage.model.Categoria;
import java.sql.SQLException;
import java.util.List;

public interface CategoriaDAO {

    List<Categoria> findDisponibiliPerUtente(String email) throws SQLException;
}
