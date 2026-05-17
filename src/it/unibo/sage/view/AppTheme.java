package it.unibo.sage.view;

import java.awt.*;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager;

/**
 * Tema visuale centralizzato.
 * Se FlatLaf e' presente in lib/, viene usato automaticamente.
 */
public final class AppTheme {

    private static final Logger LOGGER = Logger.getLogger(AppTheme.class.getName());

    public static final Color BACKGROUND = new Color(241, 246, 250);
    public static final Color BACKGROUND_TOP = new Color(249, 252, 255);
    public static final Color BACKGROUND_BOTTOM = new Color(229, 240, 244);
    public static final Color BACKGROUND_WASH = new Color(218, 244, 238, 130);
    public static final Color BACKGROUND_WASH_FADE = new Color(255, 255, 255, 0);
    public static final Color BACKGROUND_COOL_GLOW = new Color(230, 235, 255, 95);
    public static final Color BACKGROUND_WARM_GLOW = new Color(255, 244, 214, 76);
    public static final Color SURFACE = Color.WHITE;
    public static final Color GLASS_TOP = new Color(255, 255, 255, 232);
    public static final Color GLASS_BOTTOM = new Color(244, 250, 248, 192);
    public static final Color GLASS_SHADOW = new Color(15, 23, 42, 20);
    public static final Color GLASS_BORDER = new Color(255, 255, 255, 155);
    public static final Color GLASS_HIGHLIGHT = new Color(255, 255, 255, 90);
    public static final Color SURFACE_MUTED = new Color(229, 239, 246);
    public static final Color PRIMARY = new Color(37, 99, 235);
    public static final Color PRIMARY_DARK = new Color(16, 31, 53);
    public static final Color PRIMARY_HOVER = new Color(29, 78, 216);
    public static final Color ACCENT = new Color(20, 184, 166);
    public static final Color ACCENT_HOVER = new Color(13, 148, 136);
    public static final Color SIDEBAR_TOP = new Color(12, 25, 45);
    public static final Color SIDEBAR_BOTTOM = new Color(20, 45, 68);
    public static final Color SIDEBAR_BUTTON = new Color(255, 255, 255, 22);
    public static final Color SIDEBAR_BUTTON_HOVER = new Color(255, 255, 255, 42);
    public static final Color SIDEBAR_BUTTON_SELECTED = new Color(20, 184, 166, 185);
    public static final Color SIDEBAR_CAPTION = new Color(181, 220, 216);
    public static final Color SIDEBAR_SECTION = new Color(154, 169, 188);
    public static final Color SIDEBAR_ORB_PRIMARY = new Color(37, 99, 235, 38);
    public static final Color SIDEBAR_ORB_ACCENT = new Color(20, 184, 166, 28);
    public static final Color SIDEBAR_USER_TOP = new Color(255, 255, 255, 36);
    public static final Color SIDEBAR_USER_BOTTOM = new Color(255, 255, 255, 20);
    public static final Color TEXT = new Color(23, 37, 54);
    public static final Color TEXT_MUTED = new Color(87, 102, 119);
    public static final Color BORDER = new Color(206, 219, 229);
    public static final Color INCOME = new Color(22, 163, 74);
    public static final Color EXPENSE = new Color(225, 77, 91);
    public static final Color BUDGET = new Color(217, 119, 6);
    public static final Color BADGE_BACKGROUND = new Color(230, 249, 246);
    public static final Color BADGE_TEXT = new Color(13, 148, 136);
    public static final Color AVATAR_BACKGROUND = new Color(236, 253, 245);
    public static final Color AVATAR_TEXT = new Color(13, 148, 136);
    public static final Color BUTTON_BORDER = new Color(255, 255, 255, 38);

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
            LOGGER.log(Level.WARNING, "Impossibile installare il look and feel di sistema.", e);
        }
    }
}
