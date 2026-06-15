package it.unibo.sage.service;

import it.unibo.sage.model.Budget;
import it.unibo.sage.model.Categoria;
import it.unibo.sage.model.Fonte;
import it.unibo.sage.model.SpesaRicorrente;
import it.unibo.sage.model.Tag;
import it.unibo.sage.model.Transazione;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DashboardData {

    private final List<Transazione> transazioni;
    private final List<Budget> budget;
    private final List<SpesaRicorrente> ricorrenze;
    private final List<Categoria> categorie;
    private final List<Tag> tag;
    private final List<Fonte> fonti;
    private final Map<Long, String> nomiCategorie;

    public DashboardData(final List<Transazione> transazioni,
            final List<Budget> budget,
            final List<SpesaRicorrente> ricorrenze,
            final List<Categoria> categorie,
            final List<Tag> tag,
            final List<Fonte> fonti,
            final Map<Long, String> nomiCategorie) {
        this.transazioni = copyList(transazioni);
        this.budget = copyList(budget);
        this.ricorrenze = copyList(ricorrenze);
        this.categorie = copyList(categorie);
        this.tag = copyList(tag);
        this.fonti = copyList(fonti);
        this.nomiCategorie = Collections.unmodifiableMap(new HashMap<>(nomiCategorie));
    }

    public static DashboardData empty() {
        return new DashboardData(List.of(), List.of(), List.of(), List.of(),
                List.of(), List.of(), Map.of());
    }

    public List<Transazione> getTransazioni() {
        return transazioni;
    }

    public List<Budget> getBudget() {
        return budget;
    }

    public List<SpesaRicorrente> getRicorrenze() {
        return ricorrenze;
    }

    public List<Categoria> getCategorie() {
        return categorie;
    }

    public List<Tag> getTag() {
        return tag;
    }

    public List<Fonte> getFonti() {
        return fonti;
    }

    public Map<Long, String> getNomiCategorie() {
        return nomiCategorie;
    }

    private static <T> List<T> copyList(final List<T> source) {
        return Collections.unmodifiableList(new ArrayList<>(source));
    }
}
