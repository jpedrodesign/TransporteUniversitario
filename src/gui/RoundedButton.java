package gui;

import javax.swing.JButton;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/** Botão leve com cantos arredondados e resposta visual ao mouse. */
@SuppressWarnings("serial")
final class RoundedButton extends JButton {

    private final Color normal;
    private final Color hover;
    private final Color pressed;
    private final Color borderNormal;
    private final Color borderHover;
    private boolean mouseOver;

    RoundedButton(String text, Color background, Color foreground) {
        super(text);
        normal = background;
        hover = clarear(background, 0.10f);
        pressed = escurecer(background, 0.12f);
        borderNormal = UiTheme.bordaContraste(background);
        borderHover = escurecer(borderNormal, 0.12f);
        setForeground(foreground != null ? foreground : UiTheme.corContraste(background));
        setFont(UiTheme.FONT_BOLD.deriveFont(12f));
        setMargin(UiTheme.scaledInsets(5, 10, 5, 10));
        setBorderPainted(false);
        setContentAreaFilled(false);
        setFocusPainted(true);
        setOpaque(false);
        setRolloverEnabled(true);
        setUI(new BasicButtonUI());
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { mouseOver = true; repaint(); }
            @Override public void mouseExited(MouseEvent e) { mouseOver = false; repaint(); }
        });
    }

    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        Color fundo = getModel().isPressed() ? pressed : mouseOver ? hover : normal;
        Color borda = getModel().isPressed() ? escurecer(borderNormal, 0.08f) : mouseOver ? borderHover : borderNormal;

        g2.setColor(fundo);
        int arc = UiTheme.scale(11);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);

        g2.setColor(borda);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);

        if (isFocusOwner()) {
            Color foco = new Color(32, 120, 212, 170);
            g2.setColor(foco);
            g2.setStroke(new java.awt.BasicStroke(UiTheme.scale(2)));
            g2.drawRoundRect(UiTheme.scale(1), UiTheme.scale(1),
                    Math.max(0, getWidth() - UiTheme.scale(3)),
                    Math.max(0, getHeight() - UiTheme.scale(3)),
                    arc, arc);
        }
        g2.dispose();
        super.paintComponent(g);
    }

    private static Color clarear(Color color, float amount) {
        return misturar(color, Color.WHITE, amount);
    }

    private static Color escurecer(Color color, float amount) {
        return misturar(color, Color.BLACK, amount);
    }

    private static Color misturar(Color a, Color b, float amount) {
        return new Color((int) (a.getRed() * (1 - amount) + b.getRed() * amount),
                (int) (a.getGreen() * (1 - amount) + b.getGreen() * amount),
                (int) (a.getBlue() * (1 - amount) + b.getBlue() * amount));
    }
}
