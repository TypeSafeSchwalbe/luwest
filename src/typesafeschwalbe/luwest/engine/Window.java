
package typesafeschwalbe.luwest.engine;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.event.*;
import java.util.HashSet;

import javax.swing.JFrame;
import javax.swing.JPanel;

import typesafeschwalbe.luwest.math.Vec2;

public class Window {

    private final JFrame frame;
    private final JPanel panel;
    private BufferedImage buffer = new BufferedImage(
        16, 16, BufferedImage.TYPE_INT_ARGB
    );
    private Graphics2D graphics = this.buffer.createGraphics();

    private HashSet<Integer> pressedKeys = new HashSet<>();
    private StringBuffer typedText = new StringBuffer();

    private HashSet<Integer> pressedButtons = new HashSet<>();
    private Vec2 mousePosition = new Vec2();
    private int scrollOffset = 0;

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

    public Window(String title, int width, int height) {
        this.frame = new JFrame(title);
        this.panel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(buffer, 0, 0, null); // => buffer in Window
            }
        };
        this.frame.add(this.panel);
        this.frame.setSize(width, height);
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.frame.setLocationRelativeTo(null);
        this.frame.setVisible(true);
        this.frame.addKeyListener(new KeyListener() {
            @Override public void keyTyped(KeyEvent e) {
                char typed = e.getKeyChar();
                // => typedText in Window
                if(typed != '\b') {
                    typedText.append(typed);
                    return;
                }
                if(typedText.length() > 0) {
                    typedText.deleteCharAt(typedText.length() - 1);
                }
            }
            @Override public void keyPressed(KeyEvent e) {
                pressedKeys.add(e.getKeyCode()); // => pressedKeys in Window
            }
            @Override public void keyReleased(KeyEvent e) {
                pressedKeys.remove(e.getKeyCode()); // => pressedKeys in Window
            }
        });
        this.frame.addWindowFocusListener(new WindowFocusListener() {
            @Override public void windowGainedFocus(WindowEvent e) {}
            @Override public void windowLostFocus(WindowEvent e) {
                pressedKeys.clear(); // => pressedKeys in Window
            }
        });
        this.frame.addMouseMotionListener(new MouseMotionListener() {
            @Override public void mouseDragged(MouseEvent e) { 
                // => mousePosition in Window
                mousePosition.x = e.getX();
                mousePosition.y = e.getY();
            }
            @Override public void mouseMoved(MouseEvent e) {
                // => mousePosition in Window
                mousePosition.x = e.getX();
                mousePosition.y = e.getY();
            }
        });
        this.frame.addMouseListener(new MouseListener() {
            @Override public void mouseClicked(MouseEvent e) {}
            @Override public void mouseEntered(MouseEvent e) {}
            @Override public void mouseExited(MouseEvent e) {}
            @Override public void mousePressed(MouseEvent e) {
                // => pressedButtons in Window
                pressedButtons.add(e.getButton());
            }
            @Override public void mouseReleased(MouseEvent e) {
                // => pressedButtons in Window
                pressedButtons.remove(e.getButton());
            }
        });
        this.frame.addMouseWheelListener(new MouseWheelListener() {
            @Override public void mouseWheelMoved(MouseWheelEvent e) {
                // => scrollOffset in Window
                scrollOffset += e.getWheelRotation();
            }
        });
        this.updateBufferSize();
    }

    public void showBuffer() {
        this.updateBufferSize();
        this.panel.repaint();
    }

    public Graphics2D gfx() { return this.graphics; }

    public int width() { return this.panel.getWidth(); }

    public int height() { return this.panel.getHeight(); }

    public boolean keyPressed(int keyCode) {
        return this.pressedKeys.contains(keyCode); 
    }

    public String typedText() { return this.typedText.toString(); }

    public void resetTypedText() {
        this.typedText.delete(0, this.typedText.length()); 
    }

    public boolean mousePressed(int button) {
        return this.pressedButtons.contains(button);
    }

    public Vec2 mousePosition() { return this.mousePosition.clone(); }

    public int scrollOffset() { return this.scrollOffset; }

    public void resetScrollOffset() {
        this.scrollOffset = 0;
    }

}
