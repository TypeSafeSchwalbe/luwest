
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


    public static void applyAll(Scene scene) {
        for(Entity entity: scene.allWith(Position.class, Velocity.class)) {
            Position position = entity.get(Position.class);
            Velocity velocity = entity.get(Velocity.class);
            position.value.add(velocity.value.mul(Engine.deltaTime()));
        }
    }

}