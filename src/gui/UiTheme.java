package gui;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import java.awt.Color;
import java.awt.Font;

/** Identidade visual compartilhada por toda a aplicação. */
public final class UiTheme {

    public static final Color BACKGROUND = new Color(243, 246, 250);
    public static final Color SURFACE = Color.WHITE;
    public static final Color PRIMARY = new Color(25, 94, 166);
    public static final Color PRIMARY_DARK = new Color(17, 67, 119);
    public static final Color ACCENT = new Color(22, 138, 91);
    public static final Color DANGER = new Color(190, 53, 53);
    public static final Color TEXT = new Color(31, 42, 55);
    public static final Color TEXT_MUTED = new Color(99, 115, 129);
    public static final Color BORDER = new Color(218, 226, 235);
    public static final Font FONT = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD = FONT.deriveFont(Font.BOLD);

    private UiTheme() {
    }

    public static void aplicar() {
        UIManager.put("defaultFont", FONT);
        UIManager.put("Label.font", FONT);
        UIManager.put("Button.font", FONT_BOLD);
        UIManager.put("Menu.font", FONT);
        UIManager.put("MenuItem.font", FONT);
        UIManager.put("CheckBoxMenuItem.font", FONT);
        UIManager.put("TextField.font", FONT);
        UIManager.put("TextArea.font", FONT);
        UIManager.put("ComboBox.font", FONT);
        UIManager.put("Spinner.font", FONT);
        UIManager.put("Tree.font", FONT);
        UIManager.put("Panel.background", BACKGROUND);
        UIManager.put("OptionPane.background", SURFACE);
        UIManager.put("ProgressBar.foreground", ACCENT);
        UIManager.put("ProgressBar.background", BORDER);
    }

    public static Border cardBorder(String title) {
        Border titleBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER), title, 0, 0, FONT_BOLD, TEXT);
        return new CompoundBorder(titleBorder, BorderFactory.createEmptyBorder(8, 10, 10, 10));
    }

    public static void card(JComponent component, String title) {
        component.setOpaque(true);
        component.setBackground(SURFACE);
        component.setBorder(cardBorder(title));
    }
}
