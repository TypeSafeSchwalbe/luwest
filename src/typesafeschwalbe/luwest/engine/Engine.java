
package typesafeschwalbe.luwest.engine;

public class Engine {

    private static Engine instance = null;

    private static Engine getInstance() {
        if(Engine.instance == null) {
            throw new RuntimeException("Engine has not yet been initialized!");
        }
        return Engine.instance;
    }

    public static void init(String windowTitle) {
        Engine.instance = new Engine(windowTitle);
    }

    public static void startScene(Scene scene) {
        Engine.getInstance().scene = scene;
    }


    private final Window window;
    private Scene scene;

    private Engine(String windowTitle) {
        this.window = new Window(windowTitle, 854, 480);
        this.scene = new Scene();
    }

}