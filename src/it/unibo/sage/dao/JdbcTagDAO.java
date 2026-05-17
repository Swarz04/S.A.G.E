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
            "SELECT ID_Tag, Nome, is_system, Email_Proprietario "
            + "FROM TAG "
            + "WHERE is_system = TRUE OR Email_Proprietario = ? "
            + "ORDER BY is_system DESC, Nome";

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
                resultSet.getString("Email_Proprietario"));
    }
}
