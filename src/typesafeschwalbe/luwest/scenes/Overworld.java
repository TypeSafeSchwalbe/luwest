
package typesafeschwalbe.luwest.scenes;

import typesafeschwalbe.luwest.engine.Engine;
import typesafeschwalbe.luwest.engine.Entity;
import typesafeschwalbe.luwest.engine.Scene;

public class Overworld {

    public static class Hunger {
        float value = 0;
    }

    public static void decreaseHunger(Scene scene) {
        for(Entity e: scene.allWith(Hunger.class)) {
            Hunger h = e.get(Hunger.class);
            h.value += Engine.deltaTime();
        }
    }

    public static Scene createScene() {
        return new Scene("overworld.json")
            .with(new Entity().with(Hunger.class, new Hunger()))
            .with(Overworld::decreaseHunger);
    }

}