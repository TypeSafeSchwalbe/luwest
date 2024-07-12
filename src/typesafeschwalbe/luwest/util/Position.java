
package typesafeschwalbe.luwest.util;

import typesafeschwalbe.luwest.math.Vec2;

public class Position {
    public Vec2 value;

    public Position() {
        this.value = new Vec2();
    }

    public Position(Vec2 value) {
        this.value = value;
    }
}
