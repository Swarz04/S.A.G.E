package it.unibo.sage.test;

import it.unibo.sage.dao.CategoriaDAO;
import it.unibo.sage.dao.FonteDAO;
import it.unibo.sage.dao.JdbcCategoriaDAO;
import it.unibo.sage.dao.JdbcFonteDAO;
import it.unibo.sage.dao.JdbcTagDAO;
import it.unibo.sage.dao.JdbcTransazioneDAO;
import it.unibo.sage.dao.TagDAO;
import it.unibo.sage.dao.TransazioneDAO;
import it.unibo.sage.model.Categoria;
import it.unibo.sage.model.Fonte;
import it.unibo.sage.model.Tag;
import it.unibo.sage.model.Transazione;
import it.unibo.sage.utils.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public final class DatabaseContentSmokeTest {

    private static final String DEMO_EMAIL = "studente1@mail.com";

    private DatabaseContentSmokeTest() {
    }

    public static void runAll() throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            testTabelleFondamentaliPopolate(connection);
            testClassificazioniDisponibili(connection);
            testQueryTransazioniPerCategoriaTagFonte(connection);
        }
    }

    private static void testTabelleFondamentaliPopolate(final Connection connection) throws SQLException {
        assertTableHasRows(connection, "UTENTE");
        assertTableHasRows(connection, "CATEGORIA");
        assertTableHasRows(connection, "TAG");
        assertTableHasRows(connection, "FONTE");
        assertTableHasRows(connection, "TRANSIZIONE");
    }

    private static void testClassificazioniDisponibili(final Connection connection) throws SQLException {
        final CategoriaDAO categoriaDAO = new JdbcCategoriaDAO(connection);
        final TagDAO tagDAO = new JdbcTagDAO(connection);
        final FonteDAO fonteDAO = new JdbcFonteDAO(connection);

        TestAssertions.assertFalse(categoriaDAO.findDisponibiliPerUtente(DEMO_EMAIL).isEmpty(),
                "L'utente demo deve vedere almeno una categoria");
        TestAssertions.assertFalse(tagDAO.findDisponibiliPerUtente(DEMO_EMAIL).isEmpty(),
                "L'utente demo deve vedere almeno un tag");
        TestAssertions.assertFalse(fonteDAO.findDisponibiliPerUtente(DEMO_EMAIL).isEmpty(),
                "L'utente demo deve vedere almeno una fonte");
    }

    private static void testQueryTransazioniPerCategoriaTagFonte(final Connection connection) throws SQLException {
        final TransazioneDAO transazioneDAO = new JdbcTransazioneDAO(connection);
        final Categoria categoria = new JdbcCategoriaDAO(connection).findDisponibiliPerUtente(DEMO_EMAIL).get(0);
        final Tag tag = new JdbcTagDAO(connection).findDisponibiliPerUtente(DEMO_EMAIL).get(0);
        final Fonte fonte = new JdbcFonteDAO(connection).findDisponibiliPerUtente(DEMO_EMAIL).get(0);

        final List<Transazione> perCategoria = transazioneDAO.findByCategoria(DEMO_EMAIL, categoria.getId());
        for (final Transazione transazione : perCategoria) {
            TestAssertions.assertEquals(categoria.getId(), transazione.getIdCategoria(),
                    "La query per categoria deve restituire solo transazioni coerenti");
        }

        final List<Transazione> perTag = transazioneDAO.findByTag(DEMO_EMAIL, tag.getId());
        for (final Transazione transazione : perTag) {
            TestAssertions.assertEquals(DEMO_EMAIL, transazione.getEmail(),
                    "La query per tag deve restituire solo transazioni dell'utente richiesto");
        }

        final List<Transazione> perFonte = transazioneDAO.findByFonte(DEMO_EMAIL, fonte.getId());
        for (final Transazione transazione : perFonte) {
            TestAssertions.assertEquals(fonte.getId(), transazione.getIdFonte(),
                    "La query per fonte deve restituire solo entrate coerenti");
        }
    }

    private static void assertTableHasRows(final Connection connection, final String table) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM " + table);
                ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            TestAssertions.assertTrue(resultSet.getInt(1) > 0,
                    "La tabella " + table + " dovrebbe contenere dati di test/demo");
        }
    }
}
