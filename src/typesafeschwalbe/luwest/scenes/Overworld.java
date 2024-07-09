
package typesafeschwalbe.luwest.scenes;

import typesafeschwalbe.luwest.engine.Entity;
import typesafeschwalbe.luwest.engine.Scene;

public class Overworld extends Scene {

    public static class Hunger {
        float value = 0;
    }

    public Overworld() {
        super("overworld.json");
        this.with(new Entity().with(Hunger.class, new Hunger()));
        this.with(scene -> {
            for(Entity e: scene.allWith(Hunger.class)) {
                Hunger h = e.get(Hunger.class);
                h.value += 0.1;
            }
        });
    }

}