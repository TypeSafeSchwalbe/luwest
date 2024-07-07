
package typesafeschwalbe.luwest.engine;

import typesafeschwalbe.luwest.math.Vec2;

public class Sector {
 
    public static record PropInstance(Prop type, Vec2 position) {}

    PropInstance[] objects;
    Tile[][] tiles;

    public Sector() {
        
    }

}