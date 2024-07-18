
package typesafeschwalbe.luwest.util;

import typesafeschwalbe.luwest.engine.*;
import typesafeschwalbe.luwest.math.Vec2;

public class Velocity {
    
    public Vec2 value;

    public Velocity() {
        this.value = new Vec2();
    }

    public Velocity(Vec2 value) {
        this.value = value;
    }


    private static void applyAll(Scene scene) {
        for(Entity entity: scene.allWith(Position.class, Velocity.class)) {
            Position position = entity.get(Position.class);
            Velocity velocity = entity.get(Velocity.class);
            position.value.add(velocity.value.mul(Engine.deltaTime()));
        }
    }

    public static final double FRICTION = 0.1;
    public static final double DEADZONE = 0.001;

    private static void reduceAll(Scene scene) {
        for(Entity entity: scene.allWith(Velocity.class)) {
            Velocity velocity = entity.get(Velocity.class);
            velocity.value.mul(1 - Velocity.FRICTION * Engine.deltaTime());
            if(velocity.value.len() < Velocity.DEADZONE) {
                velocity.value.set(0, 0);
            }
        }
    }

    public static void handleAll(Scene scene) {
        Velocity.applyAll(scene);
        Velocity.reduceAll(scene);
    }

}