package it.unibo.sage.model;

public class Tag {

    private long id;
    private String nome;
    private boolean system;
    private String emailProprietario;
    private String icona;

    public Tag(final long id, final String nome, final boolean system,
            final String emailProprietario) {
        this(id, nome, system, emailProprietario, null);
    }

    public Tag(final long id, final String nome, final boolean system,
            final String emailProprietario, final String icona) {
        this.id = id;
        this.nome = nome;
        this.system = system;
        this.emailProprietario = emailProprietario;
        this.icona = icona;
    }

    public long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public boolean isSystem() {
        return system;
    }

    public String getEmailProprietario() {
        return emailProprietario;
    }

    public String getIcona() {
        return icona;
    }
}
