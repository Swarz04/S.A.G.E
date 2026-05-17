package it.unibo.sage.dao;

import java.sql.SQLException;

public interface PeriodoDAO {

    long trovaOCreaPeriodo(int mese, int anno) throws SQLException;
}
