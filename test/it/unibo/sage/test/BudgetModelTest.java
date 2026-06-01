package it.unibo.sage.test;

import it.unibo.sage.model.Budget;
import it.unibo.sage.model.Periodo;
import java.math.BigDecimal;

public final class BudgetModelTest {

    private BudgetModelTest() {
    }

    public static void runAll() {
        testBudgetMensileGenerale();
        testBudgetPerCategoria();
        testPeriodo();
    }

    private static void testBudgetMensileGenerale() {
        Budget budget = new Budget(1L, "studente@test.it", 10L, null,
                new BigDecimal("800.00"), new BigDecimal("300.00"), false);
        TestAssertions.assertEquals(null, budget.getIdCategoria(),
                "Il budget mensile generale deve avere categoria null");
        TestAssertions.assertFalse(budget.isAlertSoglia(), "Alert soglia non coerente");
        TestAssertions.assertBigDecimalEquals(new BigDecimal("800.00"), budget.getImportoLimite(),
                "Importo limite budget generale non coerente");
    }

    private static void testBudgetPerCategoria() {
        Budget budget = new Budget(2L, "studente@test.it", 10L, 5L,
                new BigDecimal("120.00"), new BigDecimal("121.00"), true);
        TestAssertions.assertEquals(5L, budget.getIdCategoria(), "Categoria budget non coerente");
        TestAssertions.assertTrue(budget.isAlertSoglia(), "Budget superato dovrebbe avere alert attivo");
        TestAssertions.assertBigDecimalEquals(new BigDecimal("121.00"), budget.getTotaleSpesoAttuale(),
                "Totale speso budget non coerente");
    }

    private static void testPeriodo() {
        Periodo periodo = new Periodo(7L, 6, 2026);
        TestAssertions.assertEquals(7L, periodo.getId(), "ID periodo non coerente");
        TestAssertions.assertEquals(6, periodo.getMese(), "Mese periodo non coerente");
        TestAssertions.assertEquals(2026, periodo.getAnno(), "Anno periodo non coerente");
    }
}
