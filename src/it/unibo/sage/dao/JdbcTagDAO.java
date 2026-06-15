package it.unibo.sage.dao;

import it.unibo.sage.model.Tag;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcTagDAO implements TagDAO {

    private static final String FIND_DISPONIBILI_SQL =
            "SELECT T.ID_Tag, T.Nome, T.is_system, T.Email_Proprietario, T.Icona "
            + "FROM TAG T "
            + "WHERE (T.is_system = TRUE OR T.Email_Proprietario = ?) "
            + "AND NOT (T.is_system = FALSE AND EXISTS ("
            + "SELECT 1 FROM TAG TS "
            + "WHERE TS.is_system = TRUE "
            + "AND LOWER(TRIM(TS.Nome)) = LOWER(TRIM(T.Nome)))) "
            + "ORDER BY T.is_system DESC, T.Nome";

    private final Connection connection;

    public JdbcTagDAO(final Connection connection) {
        this.connection = connection;
    }

    @Override
    public List<Tag> findDisponibiliPerUtente(final String email) throws SQLException {
        final List<Tag> tags = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(FIND_DISPONIBILI_SQL)) {
            statement.setString(1, email);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    tags.add(mapTag(resultSet));
                }
            }
        }
        return tags;
    }

    private Tag mapTag(final ResultSet resultSet) throws SQLException {
        return new Tag(
                resultSet.getLong("ID_Tag"),
                resultSet.getString("Nome"),
                resultSet.getBoolean("is_system"),
                resultSet.getString("Email_Proprietario"),
                resultSet.getString("Icona"));
    }
}
