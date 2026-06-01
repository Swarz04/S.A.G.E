package it.unibo.sage.test;

public final class TestSuite {

    private TestSuite() {
    }

    public static void main(final String[] args) throws Exception {
        int passed = 0;

        run("ModelMappingTest", () -> ModelMappingTest.runAll());
        passed++;

        run("UtenteModelTest", () -> UtenteModelTest.runAll());
        passed++;

        run("TransazioneModelTest", () -> TransazioneModelTest.runAll());
        passed++;

        run("BudgetModelTest", () -> BudgetModelTest.runAll());
        passed++;

        run("DocumentoModelTest", () -> DocumentoModelTest.runAll());
        passed++;

        run("ClassificationModelTest", () -> ClassificationModelTest.runAll());
        passed++;

        run("SpeseRicorrentiServiceTest", () -> SpeseRicorrentiServiceTest.runAll());
        passed++;

        run("ConfigurazioneInizialeMappingTest", () -> ConfigurazioneInizialeMappingTest.runAll());
        passed++;

        run("DaoContractTest", () -> DaoContractTest.runAll());
        passed++;

        run("DashboardSourceInspectionTest", () -> DashboardSourceInspectionTest.runAll());
        passed++;

        run("IconResourceTest", () -> IconResourceTest.runAll());
        passed++;

        run("SqlScriptConsistencyTest", () -> SqlScriptConsistencyTest.runAll());
        passed++;

        if (Boolean.getBoolean("sage.db.tests")) {
            run("DatabaseConnectionSmokeTest", () -> DatabaseConnectionSmokeTest.runAll());
            passed++;

            run("DatabaseContentSmokeTest", () -> DatabaseContentSmokeTest.runAll());
            passed++;
        } else {
            System.out.println("[SKIP] DatabaseConnectionSmokeTest: usare -Dsage.db.tests=true per abilitarlo");
            System.out.println("[SKIP] DatabaseContentSmokeTest: usare -Dsage.db.tests=true per abilitarlo");
        }

        System.out.println("Test suite completata. Gruppi test superati: " + passed);
    }

    private static void run(final String name, final TestCase testCase) throws Exception {
        System.out.print("[RUN] " + name + " ... ");
        testCase.run();
        System.out.println("OK");
    }

    @FunctionalInterface
    private interface TestCase {
        void run() throws Exception;
    }
}
