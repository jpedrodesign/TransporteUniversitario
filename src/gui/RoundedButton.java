package gui;

import javax.swing.JButton;
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
    private boolean mouseOver;

    RoundedButton(String text, Color background, Color foreground) {
        super(text);
        normal = background;
        hover = clarear(background, 0.10f);
        pressed = escurecer(background, 0.12f);
        setForeground(foreground);
        setFont(UiTheme.FONT_BOLD);
        setMargin(new Insets(7, 12, 7, 12));
        setBorderPainted(false);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { mouseOver = true; repaint(); }
            @Override public void mouseExited(MouseEvent e) { mouseOver = false; repaint(); }
        });
    }

    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getModel().isPressed() ? pressed : mouseOver ? hover : normal);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
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
