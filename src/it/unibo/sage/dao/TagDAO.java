package it.unibo.sage.dao;

import it.unibo.sage.model.Tag;
import java.sql.SQLException;
import java.util.List;

public interface TagDAO {

    List<Tag> findDisponibiliPerUtente(String email) throws SQLException;
}
