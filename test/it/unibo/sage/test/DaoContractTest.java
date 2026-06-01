package it.unibo.sage.test;

import it.unibo.sage.dao.CategoriaDAO;
import it.unibo.sage.dao.FonteDAO;
import it.unibo.sage.dao.SpesaRicorrenteDAO;
import it.unibo.sage.dao.TagDAO;
import it.unibo.sage.dao.TransazioneDAO;
import it.unibo.sage.model.Documento;
import it.unibo.sage.model.SpesaRicorrente;
import it.unibo.sage.model.Transazione;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;

public final class DaoContractTest {

    private DaoContractTest() {
    }

    public static void runAll() throws NoSuchMethodException {
        testTransazioneDaoEsponeMetodiUsatiDallaUi();
        testSpesaRicorrenteDaoEsponeContrattoCompleto();
        testDaoClassificazioniEspongonoCaricamentoUtente();
    }

    private static void testTransazioneDaoEsponeMetodiUsatiDallaUi() throws NoSuchMethodException {
        assertMethod(TransazioneDAO.class, "findByCategoria", String.class, long.class);
        assertMethod(TransazioneDAO.class, "findByTag", String.class, long.class);
        assertMethod(TransazioneDAO.class, "findByFonte", String.class, long.class);
        assertMethod(TransazioneDAO.class, "findByPeriodo", String.class, LocalDate.class, LocalDate.class);
        assertMethod(TransazioneDAO.class, "inserisci", Transazione.class);
        assertMethod(TransazioneDAO.class, "inserisciDocumento", Documento.class);
        assertMethod(TransazioneDAO.class, "associaTag", long.class, long.class);
        assertMethod(TransazioneDAO.class, "findTagIds", long.class, String.class);
        assertMethod(TransazioneDAO.class, "aggiorna", Transazione.class);
        assertMethod(TransazioneDAO.class, "elimina", long.class, String.class);
    }

    private static void testSpesaRicorrenteDaoEsponeContrattoCompleto() throws NoSuchMethodException {
        assertMethod(SpesaRicorrenteDAO.class, "findByEmail", String.class);
        assertMethod(SpesaRicorrenteDAO.class, "findScadute", String.class, LocalDate.class);
        assertMethod(SpesaRicorrenteDAO.class, "inserisci", SpesaRicorrente.class);
        assertMethod(SpesaRicorrenteDAO.class, "aggiornaProssimaScadenza", long.class, String.class, LocalDate.class);
        assertMethod(SpesaRicorrenteDAO.class, "elimina", long.class, String.class);
    }

    private static void testDaoClassificazioniEspongonoCaricamentoUtente() throws NoSuchMethodException {
        assertMethod(CategoriaDAO.class, "findDisponibiliPerUtente", String.class);
        assertMethod(TagDAO.class, "findDisponibiliPerUtente", String.class);
        assertMethod(FonteDAO.class, "findDisponibiliPerUtente", String.class);
    }

    private static Method assertMethod(final Class<?> type, final String name,
            final Class<?>... parameterTypes) throws NoSuchMethodException {
        final Method method = type.getMethod(name, parameterTypes);
        TestAssertions.assertTrue(method != null, "Metodo DAO mancante: " + type.getSimpleName() + "." + name);
        return method;
    }
}
