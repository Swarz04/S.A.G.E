package it.unibo.sage.service;

import it.unibo.sage.model.Budget;
import it.unibo.sage.model.TipoTransazione;
import it.unibo.sage.model.Transazione;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DashboardOverviewCalculator {

    private static final int EXPENSE_DISTRIBUTION_LIMIT = 5;

    public List<Transazione> filterTransactions(final List<Transazione> transazioni,
            final OverviewFilter filter) {
        if (filter == OverviewFilter.ALL) {
            return transazioni;
        }

        final LocalDate today = LocalDate.now();
        final List<Transazione> filtered = new ArrayList<>();
        for (final Transazione transazione : transazioni) {
            final LocalDate date = transazione.getData();
            if (date == null) {
                continue;
            }
            if (filter == OverviewFilter.MONTH
                    && date.getYear() == today.getYear()
                    && date.getMonthValue() == today.getMonthValue()) {
                filtered.add(transazione);
            } else if (filter == OverviewFilter.YEAR
                    && date.getYear() == today.getYear()) {
                filtered.add(transazione);
            }
        }
        return filtered;
    }

    public String periodDescription(final OverviewFilter filter) {
        final LocalDate today = LocalDate.now();
        if (filter == OverviewFilter.MONTH) {
            return monthName(YearMonth.from(today)) + " " + today.getYear();
        }
        if (filter == OverviewFilter.YEAR) {
            return "Anno " + today.getYear();
        }
        return "Intero storico disponibile";
    }

    public BigDecimal sumByType(final List<Transazione> transazioni,
            final TipoTransazione tipo) {
        BigDecimal totale = BigDecimal.ZERO;
        for (final Transazione transazione : transazioni) {
            if (transazione.getTipo() == tipo) {
                totale = totale.add(transazione.getImporto());
            }
        }
        return totale;
    }

    public String calculateBudgetUsage(final List<Budget> budgets) {
        for (final Budget budget : budgets) {
            final BigDecimal limite = budget.getImportoLimite();
            if (budget.getIdCategoria() == null && limite != null && limite.signum() > 0) {
                return budget.getTotaleSpesoAttuale()
                        .multiply(BigDecimal.valueOf(100))
                        .divide(limite, 0, java.math.RoundingMode.HALF_UP)
                        + "%";
            }
        }
        return "0%";
    }

    public List<DayExpense> buildDailyExpenses(final List<Transazione> transazioni,
            final YearMonth month) {
        final BigDecimal[] incomes = new BigDecimal[month.lengthOfMonth()];
        final BigDecimal[] expenses = new BigDecimal[month.lengthOfMonth()];
        for (int i = 0; i < month.lengthOfMonth(); i++) {
            incomes[i] = BigDecimal.ZERO;
            expenses[i] = BigDecimal.ZERO;
        }

        for (final Transazione transazione : transazioni) {
            if (transazione.getData() == null
                    || !YearMonth.from(transazione.getData()).equals(month)) {
                continue;
            }

            final int dayIndex = transazione.getData().getDayOfMonth() - 1;
            if (transazione.getTipo() == TipoTransazione.ENTRATA) {
                incomes[dayIndex] = incomes[dayIndex].add(transazione.getImporto());
            } else {
                expenses[dayIndex] = expenses[dayIndex].add(transazione.getImporto());
            }
        }

        final List<DayExpense> result = new ArrayList<>();
        for (int i = 0; i < month.lengthOfMonth(); i++) {
            result.add(new DayExpense(i + 1, incomes[i], expenses[i]));
        }
        return result;
    }

    public List<MonthTotals> buildYearMonthlyTotals(final List<Transazione> transazioni,
            final int year) {
        final List<MonthTotals> result = new ArrayList<>();
        for (int monthNumber = 1; monthNumber <= 12; monthNumber++) {
            final YearMonth currentMonth = YearMonth.of(year, monthNumber);
            result.add(buildMonthTotals(transazioni, currentMonth));
        }
        return result;
    }

    public List<MonthTotals> buildMonthlyTotals(final List<Transazione> transazioni,
            final int months) {
        final LocalDate referenceDate = transazioni.stream()
                .map(Transazione::getData)
                .filter(date -> date != null)
                .max(Comparator.naturalOrder())
                .orElse(LocalDate.now());
        final YearMonth end = YearMonth.from(referenceDate);
        final List<MonthTotals> result = new ArrayList<>();
        for (int i = months - 1; i >= 0; i--) {
            result.add(buildMonthTotals(transazioni, end.minusMonths(i)));
        }
        return result;
    }

    public LinkedHashMap<String, BigDecimal> buildExpenseDistribution(
            final List<Transazione> transazioni,
            final Map<Long, String> categoryNames) {
        final Map<String, BigDecimal> totals = new HashMap<>();
        for (final Transazione transazione : transazioni) {
            if (transazione.getTipo() != TipoTransazione.SPESA) {
                continue;
            }
            final String category = transazione.getIdCategoria() == null
                    ? "Senza categoria"
                    : categoryNames.getOrDefault(
                            transazione.getIdCategoria(),
                            "Categoria " + transazione.getIdCategoria());
            totals.merge(category, transazione.getImporto(), BigDecimal::add);
        }

        final List<Map.Entry<String, BigDecimal>> sorted =
                new ArrayList<>(totals.entrySet());
        sorted.sort((left, right) -> right.getValue().compareTo(left.getValue()));

        final LinkedHashMap<String, BigDecimal> result = new LinkedHashMap<>();
        BigDecimal other = BigDecimal.ZERO;
        for (int i = 0; i < sorted.size(); i++) {
            final Map.Entry<String, BigDecimal> entry = sorted.get(i);
            if (i < EXPENSE_DISTRIBUTION_LIMIT) {
                result.put(entry.getKey(), entry.getValue());
            } else {
                other = other.add(entry.getValue());
            }
        }
        if (other.signum() > 0) {
            result.put("Altro", other);
        }
        return result;
    }

    public String monthName(final YearMonth month) {
        String label = month.getMonth().getDisplayName(TextStyle.FULL, Locale.ITALIAN);
        if (!label.isEmpty()) {
            label = Character.toUpperCase(label.charAt(0)) + label.substring(1);
        }
        return label;
    }

    private MonthTotals buildMonthTotals(final List<Transazione> transazioni,
            final YearMonth month) {
        BigDecimal income = BigDecimal.ZERO;
        BigDecimal expense = BigDecimal.ZERO;
        for (final Transazione transazione : transazioni) {
            if (transazione.getData() == null
                    || !YearMonth.from(transazione.getData()).equals(month)) {
                continue;
            }
            if (transazione.getTipo() == TipoTransazione.ENTRATA) {
                income = income.add(transazione.getImporto());
            } else {
                expense = expense.add(transazione.getImporto());
            }
        }
        return new MonthTotals(shortMonthName(month), income, expense);
    }

    private String shortMonthName(final YearMonth month) {
        String label = month.getMonth().getDisplayName(TextStyle.SHORT, Locale.ITALIAN);
        if (!label.isEmpty()) {
            label = Character.toUpperCase(label.charAt(0)) + label.substring(1).replace(".", "");
        }
        return label;
    }

    public enum OverviewFilter {
        MONTH,
        YEAR,
        ALL
    }

    public static final class DayExpense {
        private final int day;
        private final BigDecimal income;
        private final BigDecimal expense;

        private DayExpense(final int day, final BigDecimal income,
                final BigDecimal expense) {
            this.day = day;
            this.income = income;
            this.expense = expense;
        }

        public int getDay() {
            return day;
        }

        public BigDecimal getIncome() {
            return income;
        }

        public BigDecimal getExpense() {
            return expense;
        }
    }

    public static final class MonthTotals {
        private final String label;
        private final BigDecimal income;
        private final BigDecimal expense;

        private MonthTotals(final String label, final BigDecimal income,
                final BigDecimal expense) {
            this.label = label;
            this.income = income;
            this.expense = expense;
        }

        public String getLabel() {
            return label;
        }

        public BigDecimal getIncome() {
            return income;
        }

        public BigDecimal getExpense() {
            return expense;
        }
    }
}
