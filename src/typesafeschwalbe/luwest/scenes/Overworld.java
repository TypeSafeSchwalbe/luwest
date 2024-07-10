
package typesafeschwalbe.luwest.scenes;

import java.util.function.Supplier;

import typesafeschwalbe.luwest.engine.Engine;
import typesafeschwalbe.luwest.engine.Entity;
import typesafeschwalbe.luwest.engine.Scene;

public class Overworld {

    public static class Hunger {
        float value = 0;
    }

    public static Supplier<Scene> SCENE = () -> new Scene("overworld.json")
        .with(new Entity().with(Hunger.class, new Hunger()))
        .with(scene -> {
            for(Entity e: scene.allWith(Hunger.class)) {
                Hunger h = e.get(Hunger.class);
                h.value += Engine.deltaTime();
            }
        });

}