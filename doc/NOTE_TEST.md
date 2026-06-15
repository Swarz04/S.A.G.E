# Test S.A.G.E.

Sono state aggiunte classi di test Java pure, senza JUnit/Maven/Gradle, per restare coerenti con la struttura attuale del progetto.

## Cartella test

```text
test/it/unibo/sage/test/
```

## Classi principali

- `TestSuite`: esegue tutti i gruppi di test.
- `TestAssertions`: mini libreria di asserzioni.
- `ModelMappingTest`: test aggregato sui mapping principali dei model.
- `UtenteModelTest`: test specifici su `Utente` e `Ruolo`.
- `TransazioneModelTest`: test su spese/entrate e mapping `TipoTransazione`.
- `BudgetModelTest`: test su `Budget` e `Periodo`.
- `DocumentoModelTest`: test su `Documento`, `DocumentoDettaglio`, `SpesaDocumentabile`.
- `ClassificationModelTest`: test su `Categoria`, `Tag`, `Fonte`.
- `SpeseRicorrentiServiceTest`: test sulla logica delle ricorrenze e validazioni.
- `ConfigurazioneInizialeMappingTest`: test sulle mappature automatiche della configurazione iniziale.
- `DaoContractTest`: verifica che i DAO espongano i metodi usati da service e UI.
- `DashboardSourceInspectionTest`: verifica cablaggio click categoria/tag/fonte, selettori icone e nome delle ricorrenze.
- `RequestedFeaturesSourceInspectionTest`: verifica che le ricorrenze creino transazioni collegate e che i duplicati siano bloccati.
- `IconResourceTest`: verifica presenza delle icone PNG essenziali.
- `SqlScriptConsistencyTest`: verifica coerenza degli script SQL principali, delle nuove colonne e della migrazione.
- `DatabaseConnectionSmokeTest`: opzionale, richiede XAMPP/MySQL acceso.
- `DatabaseContentSmokeTest`: opzionale, richiede database popolato.

## Esecuzione senza database

Da PowerShell nella root del progetto:

```powershell
Remove-Item -Recurse -Force build -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force -Path "build\classes"

javac -cp "lib/*" -d build/classes (Get-ChildItem -Recurse -Filter *.java -Path src,test).FullName

java -cp "build/classes;lib/*" it.unibo.sage.test.TestSuite
```

Questi test non richiedono XAMPP.

## Esecuzione con database

Prima avviare MySQL in XAMPP e assicurarsi che il database S.A.G.E. sia importato e popolato.

```powershell
java -Dsage.db.tests=true -cp "build/classes;lib/*" it.unibo.sage.test.TestSuite
```

I test DB controllano connessione, tabelle popolate, categorie/tag/fonti disponibili e coerenza delle query per categoria, tag e fonte.
