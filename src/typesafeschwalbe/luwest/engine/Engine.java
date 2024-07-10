
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

    public static void setScene(Scene scene) {
        Engine.getInstance().scene = scene;
    }

    public static void start() {
        Engine.getInstance().gameloop();
    }

    public static double deltaTime() {
        return Engine.getInstance().deltaTime;
    }

    public static Window window() {
        return Engine.getInstance().window;
    }

    public static Scene scene() {
        return Engine.getInstance().scene;
    }


    private final Window window;
    private Scene scene;
    private double deltaTime;
    private double fpsTarget = 60.0;

    private Engine(String windowTitle) {
        this.window = new Window(windowTitle, 854, 480);
        this.scene = new Scene();
    }

    private void gameloop() {
        long lastTime = System.currentTimeMillis();
        for(;;) {
            long startTime = System.currentTimeMillis();
            this.deltaTime = (startTime - lastTime) / 1000.0;
            lastTime = startTime;
            this.handleFrame();
            long frameTime = (long) (1000.0 / this.fpsTarget);
            long endTime = System.currentTimeMillis();
            long waitTime = frameTime - (endTime - startTime);
            if(waitTime > 0) {
                try {
                    Thread.sleep(waitTime);
                } catch(InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    private void handleFrame() {
        this.scene.runSystems();
        this.window.showBuffer();
    }

}