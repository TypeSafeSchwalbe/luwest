
package typesafeschwalbe.luwest.engine;

public class Engine {

    private final Window window;
    public Scene scene;

    public Engine(String title, Scene scene) {
        this.window = new Window(title, 854, 480);
        this.scene = scene;
    }

}