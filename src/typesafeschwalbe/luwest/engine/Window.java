
package typesafeschwalbe.luwest.engine;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class Window {

    private final JFrame frame;
    private final JPanel panel;
    private BufferedImage buffer = new BufferedImage(
        16, 16, BufferedImage.TYPE_INT_ARGB
    );
    private Graphics2D graphics = this.buffer.createGraphics();
    private Set<Integer> pressedKeys = new HashSet<>();

    private void updateBufferSize() {
        if(this.width() == 0 || this.height() == 0) {
            return;
        }
        boolean bufferInvalid = this.width() != this.buffer.getWidth()
            || this.height() != this.buffer.getHeight();
        if(!bufferInvalid) {
            return;
        }
        this.graphics.dispose();
        BufferedImage newBuffer = new BufferedImage(
            this.width(), this.height(), BufferedImage.TYPE_INT_ARGB
        );
        this.graphics = newBuffer.createGraphics();
        this.graphics.drawImage(this.buffer, 0, 0, null);
        this.buffer = newBuffer;
    }

    public void showBuffer() {
        this.updateBufferSize();
        this.panel.repaint();
    }

    public Window(String title, int width, int height) {
        this.frame = new JFrame(title);
        this.panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // 'buffer' is a member of 'Window'
                g.drawImage(buffer, 0, 0, null);
            }
        };
        this.frame.add(this.panel);
        this.frame.setSize(width, height);
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.frame.setLocationRelativeTo(null);
        this.frame.setVisible(true);
        this.frame.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                // TODO!
            }

            @Override
            public void keyPressed(KeyEvent e) {
                // 'pressedKeys' is a member of 'Window'
                pressedKeys.add(e.getKeyCode());
            }
    
            @Override
            public void keyReleased(KeyEvent e) {
                // 'pressedKeys' is a member of 'Window'
                pressedKeys.remove(e.getKeyCode());
            }
        });
        this.updateBufferSize();
    }

    public Graphics2D gfx() { return this.graphics; }

    public int width() { return this.panel.getWidth(); }

    public int height() { return this.panel.getHeight(); }

}
