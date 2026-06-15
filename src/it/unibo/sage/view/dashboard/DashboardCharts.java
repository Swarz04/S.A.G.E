package it.unibo.sage.view.dashboard;

import it.unibo.sage.service.DashboardOverviewCalculator.DayExpense;
import it.unibo.sage.service.DashboardOverviewCalculator.MonthTotals;
import it.unibo.sage.view.theme.AppTheme;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.JPanel;

final class DashboardCharts {

    private DashboardCharts() {
    }

    static JPanel dailyExpenses(final List<DayExpense> dailyExpenses) {
        return new DailyExpenseChartPanel(dailyExpenses);
    }

    static JPanel monthlyTrend(final List<MonthTotals> monthTotals) {
        return new MonthlyTrendChartPanel(monthTotals);
    }

    static JPanel expenseDistribution(final LinkedHashMap<String, BigDecimal> expensesByCategory) {
        return new ExpenseDistributionChartPanel(expensesByCategory);
    }

    static Color chartColor(final int index) {
        final Color[] colors = {
            AppTheme.PRIMARY,
            AppTheme.ACCENT,
            AppTheme.BUDGET,
            AppTheme.EXPENSE,
            AppTheme.INCOME,
            new Color(124, 58, 237),
            new Color(14, 165, 233)
        };
        return colors[index % colors.length];
    }

    private static String formatCompactAmount(final double value) {
        if (value >= 1000.0) {
            return String.format(Locale.US, "%.1fk", value / 1000.0).replace('.', ',');
        }
        return String.format(Locale.US, "%.0f", value).replace('.', ',');
    }

    private static String formatEuro(final BigDecimal value) {
        return String.format("%.2f euro", value);
    }

    private static final class DailyExpenseChartPanel extends JPanel {
        private final List<DayExpense> dailyExpenses;

        private DailyExpenseChartPanel(final List<DayExpense> dailyExpenses) {
            this.dailyExpenses = dailyExpenses;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(final Graphics graphics) {
            super.paintComponent(graphics);
            final Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            final int width = getWidth();
            final int height = getHeight();
            final int left = 42;
            final int right = 16;
            final int top = 18;
            final int bottom = 32;
            final int chartWidth = width - left - right;
            final int chartHeight = height - top - bottom;

            double maxValue = 0.0;
            for (final DayExpense dayExpense : dailyExpenses) {
                maxValue = Math.max(maxValue, dayExpense.getIncome().doubleValue());
                maxValue = Math.max(maxValue, dayExpense.getExpense().doubleValue());
            }
            if (maxValue <= 0.0) {
                g2.setColor(AppTheme.TEXT_MUTED);
                g2.setFont(new Font("SansSerif", Font.PLAIN, 13));
                g2.drawString("Nessuna entrata o spesa registrata nel mese selezionato.", 16, height / 2);
                g2.dispose();
                return;
            }

            for (int i = 0; i <= 4; i++) {
                final int y = top + (int) Math.round(chartHeight * i / 4.0);
                g2.setColor(new Color(225, 232, 240));
                g2.drawLine(left, y, width - right, y);

                final double labelValue = maxValue * (4 - i) / 4.0;
                g2.setColor(AppTheme.TEXT_MUTED);
                g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
                g2.drawString(formatCompactAmount(labelValue), 4, y + 4);
            }

            final int days = Math.max(1, dailyExpenses.size());
            final double slotWidth = chartWidth / (double) days;
            final int barWidth = Math.max(2, Math.min(8, (int) Math.floor(slotWidth * 0.34)));
            final int chartBottom = top + chartHeight;

            for (int i = 0; i < dailyExpenses.size(); i++) {
                final DayExpense dayExpense = dailyExpenses.get(i);
                final int groupWidth = barWidth * 2 + 2;
                final int baseX = left + (int) Math.round(i * slotWidth + (slotWidth - groupWidth) / 2.0);
                final int incomeHeight = (int) Math.round(chartHeight * (dayExpense.getIncome().doubleValue() / maxValue));
                final int expenseHeight = (int) Math.round(chartHeight * (dayExpense.getExpense().doubleValue() / maxValue));

                if (incomeHeight > 0) {
                    g2.setColor(AppTheme.INCOME);
                    g2.fillRoundRect(baseX, chartBottom - incomeHeight, barWidth, incomeHeight, 5, 5);
                }
                if (expenseHeight > 0) {
                    g2.setColor(AppTheme.EXPENSE);
                    g2.fillRoundRect(baseX + barWidth + 2, chartBottom - expenseHeight,
                            barWidth, expenseHeight, 5, 5);
                }

                final boolean showLabel = dayExpense.getDay() == 1
                        || dayExpense.getDay() == dailyExpenses.size()
                        || dayExpense.getDay() % 5 == 0;
                if (showLabel) {
                    final String label = String.valueOf(dayExpense.getDay());
                    g2.setColor(AppTheme.TEXT_MUTED);
                    g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
                    final int labelWidth = g2.getFontMetrics().stringWidth(label);
                    final int labelX = left + (int) Math.round((i + 0.5) * slotWidth) - labelWidth / 2;
                    g2.drawString(label, labelX, height - 8);
                }
            }

            g2.setColor(AppTheme.INCOME);
            g2.fillRoundRect(width - 130, 4, 12, 12, 4, 4);
            g2.setColor(AppTheme.TEXT_MUTED);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
            g2.drawString("Entrate", width - 112, 14);
            g2.setColor(AppTheme.EXPENSE);
            g2.fillRoundRect(width - 62, 4, 12, 12, 4, 4);
            g2.setColor(AppTheme.TEXT_MUTED);
            g2.drawString("Spese", width - 44, 14);

            g2.dispose();
        }
    }

    private static final class MonthlyTrendChartPanel extends JPanel {
        private final List<MonthTotals> monthTotals;

        private MonthlyTrendChartPanel(final List<MonthTotals> monthTotals) {
            this.monthTotals = monthTotals;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(final Graphics graphics) {
            super.paintComponent(graphics);
            final Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            final int width = getWidth();
            final int height = getHeight();
            final int left = 42;
            final int right = 16;
            final int top = 18;
            final int bottom = 32;
            final int chartWidth = width - left - right;
            final int chartHeight = height - top - bottom;

            double maxValue = 0.0;
            for (final MonthTotals totals : monthTotals) {
                maxValue = Math.max(maxValue, totals.getIncome().doubleValue());
                maxValue = Math.max(maxValue, totals.getExpense().doubleValue());
            }
            if (maxValue <= 0.0) {
                g2.setColor(AppTheme.TEXT_MUTED);
                g2.setFont(new Font("SansSerif", Font.PLAIN, 13));
                g2.drawString("Nessun dato disponibile per il grafico.", 16, height / 2);
                g2.dispose();
                return;
            }

            g2.setColor(new Color(225, 232, 240));
            for (int i = 0; i <= 4; i++) {
                final int y = top + (int) Math.round(chartHeight * i / 4.0);
                g2.drawLine(left, y, width - right, y);
                final double labelValue = maxValue * (4 - i) / 4.0;
                g2.setColor(AppTheme.TEXT_MUTED);
                g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
                g2.drawString(formatCompactAmount(labelValue), 4, y + 4);
                g2.setColor(new Color(225, 232, 240));
            }

            final int months = Math.max(1, monthTotals.size());
            final int groupWidth = Math.max(32, chartWidth / months);
            final int barWidth = Math.max(10, Math.min(24, (groupWidth - 12) / 2));
            final int chartBottom = top + chartHeight;

            for (int i = 0; i < monthTotals.size(); i++) {
                final MonthTotals totals = monthTotals.get(i);
                final int baseX = left + i * groupWidth + Math.max(6, (groupWidth - (barWidth * 2 + 6)) / 2);
                final int incomeHeight = (int) Math.round(chartHeight * (totals.getIncome().doubleValue() / maxValue));
                final int expenseHeight = (int) Math.round(chartHeight * (totals.getExpense().doubleValue() / maxValue));

                g2.setColor(AppTheme.INCOME);
                g2.fillRoundRect(baseX, chartBottom - incomeHeight, barWidth, incomeHeight, 8, 8);
                g2.setColor(AppTheme.EXPENSE);
                g2.fillRoundRect(baseX + barWidth + 6, chartBottom - expenseHeight, barWidth, expenseHeight, 8, 8);

                g2.setColor(AppTheme.TEXT_MUTED);
                g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
                final int labelWidth = g2.getFontMetrics().stringWidth(totals.getLabel());
                final int labelX = left + i * groupWidth + (groupWidth - labelWidth) / 2;
                g2.drawString(totals.getLabel(), labelX, height - 8);
            }

            g2.setColor(AppTheme.INCOME);
            g2.fillRoundRect(width - 130, 4, 12, 12, 4, 4);
            g2.setColor(AppTheme.TEXT_MUTED);
            g2.drawString("Entrate", width - 112, 14);
            g2.setColor(AppTheme.EXPENSE);
            g2.fillRoundRect(width - 62, 4, 12, 12, 4, 4);
            g2.setColor(AppTheme.TEXT_MUTED);
            g2.drawString("Spese", width - 44, 14);

            g2.dispose();
        }
    }

    private static final class ExpenseDistributionChartPanel extends JPanel {
        private final List<Map.Entry<String, BigDecimal>> slices;

        private ExpenseDistributionChartPanel(final LinkedHashMap<String, BigDecimal> expensesByCategory) {
            this.slices = new ArrayList<>(expensesByCategory.entrySet());
            setOpaque(false);
        }

        @Override
        protected void paintComponent(final Graphics graphics) {
            super.paintComponent(graphics);
            final Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            if (slices.isEmpty()) {
                g2.setColor(AppTheme.TEXT_MUTED);
                g2.setFont(new Font("SansSerif", Font.PLAIN, 13));
                g2.drawString("Nessuna spesa disponibile.", 12, getHeight() / 2);
                g2.dispose();
                return;
            }

            final int size = Math.min(getWidth(), getHeight()) - 28;
            final int x = (getWidth() - size) / 2;
            final int y = (getHeight() - size) / 2;
            BigDecimal total = BigDecimal.ZERO;
            for (final Map.Entry<String, BigDecimal> entry : slices) {
                total = total.add(entry.getValue());
            }

            double startAngle = 90.0;
            for (int i = 0; i < slices.size(); i++) {
                final Map.Entry<String, BigDecimal> entry = slices.get(i);
                final double angle = entry.getValue().doubleValue() * 360.0 / total.doubleValue();
                g2.setColor(chartColor(i));
                g2.fillArc(x, y, size, size, (int) Math.round(startAngle), (int) -Math.round(angle));
                startAngle -= angle;
            }

            final int innerSize = (int) (size * 0.56);
            final int innerX = x + (size - innerSize) / 2;
            final int innerY = y + (size - innerSize) / 2;
            g2.setColor(new Color(255, 255, 255, 235));
            g2.fillOval(innerX, innerY, innerSize, innerSize);

            g2.setColor(AppTheme.TEXT_MUTED);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
            final String line1 = "Totale spese";
            final String line2 = formatEuro(total);
            FontMetrics fm = g2.getFontMetrics();
            final int line1Width = fm.stringWidth(line1);
            g2.drawString(line1, getWidth() / 2 - line1Width / 2, getHeight() / 2 - 6);

            g2.setColor(AppTheme.TEXT);
            g2.setFont(new Font("SansSerif", Font.BOLD, 14));
            fm = g2.getFontMetrics();
            final int line2Width = fm.stringWidth(line2);
            g2.drawString(line2, getWidth() / 2 - line2Width / 2, getHeight() / 2 + 14);

            g2.dispose();
        }
    }
}
