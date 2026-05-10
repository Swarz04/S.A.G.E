package it.unibo.sage.view;

import javax.swing.*;
import java.awt.*;

/**
 * Piccolo effetto hover per rendere i JButton piu visibili anche senza FlatLaf.
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