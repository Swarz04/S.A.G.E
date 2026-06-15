package it.unibo.sage.dao;

import it.unibo.sage.model.Fonte;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcFonteDAO implements FonteDAO {

    private static final String FIND_DISPONIBILI_SQL =
            "SELECT F.ID_Fonte, F.Nome, F.Icona, F.is_system, F.Email_Proprietario "
            + "FROM FONTE F "
            + "WHERE (F.is_system = TRUE OR F.Email_Proprietario = ?) "
            + "AND NOT (F.is_system = FALSE AND EXISTS ("
            + "SELECT 1 FROM FONTE FS "
            + "WHERE FS.is_system = TRUE "
            + "AND LOWER(TRIM(FS.Nome)) = LOWER(TRIM(F.Nome)))) "
            + "ORDER BY F.is_system DESC, F.Nome";

    private final Connection connection;

    public JdbcFonteDAO(final Connection connection) {
        this.connection = connection;
    }

    @Override
    public List<Fonte> findDisponibiliPerUtente(final String email) throws SQLException {
        final List<Fonte> fonti = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(FIND_DISPONIBILI_SQL)) {
            statement.setString(1, email);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    fonti.add(mapFonte(resultSet));
                }
            }
        }
        return fonti;
    }

    private Fonte mapFonte(final ResultSet resultSet) throws SQLException {
        return new Fonte(
                resultSet.getLong("ID_Fonte"),
                resultSet.getString("Nome"),
                resultSet.getBoolean("is_system"),
                resultSet.getString("Email_Proprietario"),
                resultSet.getString("Icona"));
    }
}
