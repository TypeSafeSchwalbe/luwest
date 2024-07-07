
package typesafeschwalbe.luwest.engine;

import typesafeschwalbe.luwest.math.Vec2;

public class Prop {
    
    public class Collider {
        public Vec2 offset;
        public Vec2 size;
    }

    public Texture sprite;
    public Vec2 size;
    public Vec2 anchor;
    public Collider[] colliders;

}
