package it.unibo.sage.dao;

import it.unibo.sage.model.Categoria;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcCategoriaDAO implements CategoriaDAO {

    private static final String FIND_DISPONIBILI_SQL =
            "SELECT C.ID_Categoria, C.Nome, C.is_system, C.Email_Proprietario, C.Icona "
            + "FROM CATEGORIA C "
            + "WHERE (C.is_system = TRUE OR C.Email_Proprietario = ?) "
            + "AND NOT (C.is_system = FALSE AND EXISTS ("
            + "SELECT 1 FROM CATEGORIA CS "
            + "WHERE CS.is_system = TRUE "
            + "AND LOWER(TRIM(CS.Nome)) = LOWER(TRIM(C.Nome)))) "
            + "ORDER BY C.is_system DESC, C.Nome";

    private final Connection connection;

    public JdbcCategoriaDAO(final Connection connection) {
        this.connection = connection;
    }

    @Override
    public List<Categoria> findDisponibiliPerUtente(final String email) throws SQLException {
        final List<Categoria> categorie = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(FIND_DISPONIBILI_SQL)) {
            statement.setString(1, email);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    categorie.add(mapCategoria(resultSet));
                }
            }
        }
        return categorie;
    }

    private Categoria mapCategoria(final ResultSet resultSet) throws SQLException {
        return new Categoria(
                resultSet.getLong("ID_Categoria"),
                resultSet.getString("Nome"),
                resultSet.getBoolean("is_system"),
                resultSet.getString("Email_Proprietario"),
                resultSet.getString("Icona"));
    }
}
