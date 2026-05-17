package it.unibo.sage.view;

import javax.swing.*;
import java.awt.*;

/**
 * Gestisce l'hover dei bottoni: e' una cosa piccola, ma rende l'interfaccia piu'
 * leggibile quando l'utente passa con il mouse.
 */
public class ButtonHoverAdapter extends java.awt.event.MouseAdapter {

    private final AbstractButton button;
    private final Color normalColor;
    private final Color hoverColor;

    public ButtonHoverAdapter(AbstractButton button, Color normalColor, Color hoverColor) {
        this.button = button;
        this.normalColor = normalColor;
        this.hoverColor = hoverColor;
    }

    @Override
    public void mouseEntered(java.awt.event.MouseEvent event) {
        button.setBackground(hoverColor);
    }

    @Override
    public void mouseExited(java.awt.event.MouseEvent event) {
        button.setBackground(normalColor);
    }
}
