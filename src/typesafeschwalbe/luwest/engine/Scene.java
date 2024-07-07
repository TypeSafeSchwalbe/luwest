
package typesafeschwalbe.luwest.engine;

import java.util.*;

public abstract class Scene {

    private int[] background;
    private Map<long[], Sector> sectors = new HashMap<>();

    public Scene(String terrain) {
        throw new RuntimeException("not yet implemented");
    }

}