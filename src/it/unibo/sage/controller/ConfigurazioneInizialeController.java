package it.unibo.sage.controller;

import it.unibo.sage.service.ConfigurazioneInizialeService;
import java.math.BigDecimal;
import java.sql.SQLException;

public class ConfigurazioneInizialeController {

    private final ConfigurazioneInizialeService configurazioneService =
            new ConfigurazioneInizialeService();

    public void completaConfigurazione(final String email, final String focusSpese,
            final String fonteEntrata, final BigDecimal budgetMensile,
            final String gruppoTag) throws SQLException {
        configurazioneService.completaConfigurazione(email, focusSpese, fonteEntrata,
                budgetMensile, gruppoTag);
    }
}
