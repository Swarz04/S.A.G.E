package it.unibo.sage.model;

public class Periodo {

    private long id;
    private int mese;
    private int anno;

    public Periodo(final long id, final int mese, final int anno) {
        this.id = id;
        this.mese = mese;
        this.anno = anno;
    }

    public long getId() {
        return id;
    }

    public int getMese() {
        return mese;
    }

    public int getAnno() {
        return anno;
    }
}
