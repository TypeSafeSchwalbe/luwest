
package typesafeschwalbe.luwest.engine;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class Window {

    private Object repaintSync = new Object();
    private final JFrame frame;
    private final JPanel panel;
    private BufferedImage buffer = new BufferedImage(
        16, 16, BufferedImage.TYPE_INT_ARGB
    );
    private Graphics2D graphics = this.buffer.createGraphics();

    private void updateBufferSize() {
        if(this.panel.getWidth() == 0 || this.panel.getHeight() == 0) {
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
        synchronized(this.repaintSync) {
            try {
                this.repaintSync.wait();
            } catch(InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public Window(String title, int width, int height) {
        this.frame = new JFrame(title);
        this.panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // 'buffer' and 'repaintSync' are members of 'Window'
                g.drawImage(buffer, 0, 0, null);
                synchronized(repaintSync) {
                    repaintSync.notifyAll();
                }
            }
        };
        this.frame.add(this.panel);
        this.frame.setSize(width, height);
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.frame.setLocationRelativeTo(null);
        this.frame.setVisible(true);
        this.updateBufferSize();
    }

    public Graphics2D gfx() { return this.graphics; }

    public int width() { return this.panel.getWidth(); }

    public int height() { return this.panel.getHeight(); }

}
