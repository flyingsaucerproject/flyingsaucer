import javax.swing.*;
import java.awt.*;

/**
 * @author patrick
 */
public class FontGlyphRender {
    public static void main(String[] args) {
        new FontGlyphRender().run();
    }

    private void run() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JFrame frame = new JFrame("Font Glyph Render");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                JPanel layout = new JPanel(new FlowLayout());
                final String glyph = "\u2220";
                JLabel label = new JLabel("Standard JLabel: " + glyph);
                layout.add(label);

                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                Font[] fonts = ge.getAllFonts();

                for (int i = 0; i < fonts.length; i++) {
                    String faceName = fonts[i].getName();
                    Font f = fonts[i].deriveFont(12F);
                    JLabel drawStringLabel = newLabel(faceName + ": " + glyph, f);
                    layout.add(drawStringLabel);
                }


                frame.getContentPane().add(BorderLayout.CENTER, layout);
                frame.pack();
                frame.setSize(1024, 768);
                frame.setVisible(true);
            }

            private JLabel newLabel(final String text, final Font font) {
                return new JLabel(text) {
                    protected void paintComponent(Graphics graphics) {
                        Graphics2D g2 = (Graphics2D) graphics;
                        g2.setFont(font);
                        g2.drawString(text, 10, 10);
                    }
                };
            }
        }

        );
    }
}
