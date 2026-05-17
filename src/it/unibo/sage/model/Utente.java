package it.unibo.sage.model;

public class Utente {

    private String email;
    private String password;
    private String nome;
    private String cognome;
    private Ruolo ruolo;

    public Utente(final String email, final String password, final String nome,
            final String cognome, final Ruolo ruolo) {
        this.email = email;
        this.password = password;
        this.nome = nome;
        this.cognome = cognome;
        this.ruolo = ruolo;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getNome() {
        return nome;
    }

    public String getCognome() {
        return cognome;
    }

    public Ruolo getRuolo() {
        return ruolo;
    }
}
