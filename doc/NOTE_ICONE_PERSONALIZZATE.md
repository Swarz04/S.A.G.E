# Icone personalizzate

## Aggiornamento del database esistente

In phpMyAdmin selezionare il database
`schema_finale_del_relazionale_sage_vista_ibrida_raffinata`, aprire **Importa**
e caricare:

`doc/sql/aggiornamento_icone_personalizzate.sql`

Lo script:

- aggiunge `FONTE.Icona`;
- porta i campi `Icona` di categorie, tag e fonti a `VARCHAR(255)`;
- assegna un'icona iniziale alle fonti gia' presenti.

## Uso nell'app

Nei pannelli **Categorie e Tag** e **Fonti**, premere **+** oppure **Modifica**.
Si puo' scegliere una delle icone predefinite oppure trascinare un'immagine nel
riquadro dedicato. Sono accettati PNG, JPG/JPEG, GIF e BMP fino a 10 MB.

Le immagini vengono normalizzate in PNG e salvate localmente nella cartella:

`%USERPROFILE%\.sage\icons`

Nel database viene salvato solo un riferimento breve `custom:...png`, non il
percorso originale scelto dall'utente.
