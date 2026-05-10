package it.unibo.sage.view;

import java.awt.*;
import java.lang.reflect.Method;
import javax.swing.UIManager;

/**
 * Tema visuale centralizzato.
 * Se FlatLaf e' presente in lib/, viene usato automaticamente.
 */
public final class AppTheme {

    public static final Color BACKGROUND = new Color(244, 247, 251);
    public static final Color SURFACE = Color.WHITE;
    public static final Color SURFACE_MUTED = new Color(232, 239, 248);
    public static final Color PRIMARY = new Color(24, 119, 242);
    public static final Color PRIMARY_DARK = new Color(18, 34, 64);
    public static final Color PRIMARY_HOVER = new Color(38, 132, 255);
    public static final Color SIDEBAR_BUTTON = new Color(31, 52, 91);
    public static final Color SIDEBAR_BUTTON_HOVER = new Color(46, 76, 130);
    public static final Color TEXT = new Color(30, 41, 59);
    public static final Color TEXT_MUTED = new Color(100, 116, 139);
    public static final Color BORDER = new Color(220, 226, 235);
    public static final Color INCOME = new Color(17, 122, 101);
    public static final Color EXPENSE = new Color(190, 70, 58);

    private AppTheme() {
    }

    public static void install() {
        if (!installFlatLaf()) {
            installSystemLookAndFeel();
        }

        UIManager.put("Button.arc", 12);
        UIManager.put("Component.arc", 12);
        UIManager.put("TextComponent.arc", 10);
        UIManager.put("defaultFont", new Font("SansSerif", Font.PLAIN, 14));
    }

    private static boolean installFlatLaf() {
        try {
            Class<?> flatLightLafClass = Class.forName("com.formdev.flatlaf.FlatLightLaf");
            Method setupMethod = flatLightLafClass.getMethod("setup");
            setupMethod.invoke(null);
            return true;
        } catch (ReflectiveOperationException e) {
            return false;
        }
    }

    private static void installSystemLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}