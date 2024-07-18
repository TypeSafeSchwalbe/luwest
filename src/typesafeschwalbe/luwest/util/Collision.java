
package typesafeschwalbe.luwest.util;

import java.util.ArrayList;
import java.util.List;

import typesafeschwalbe.luwest.engine.Scene;
import typesafeschwalbe.luwest.engine.Entity;
import typesafeschwalbe.luwest.math.Vec2;

public class Collision {

    public static class BoxCollider {
        public Vec2 offset;
        public Vec2 size;

        public BoxCollider(Vec2 offset, Vec2 size) {
            this.offset = offset;
            this.size = size;
        }

        public static boolean colliding(
            BoxCollider a, Vec2 ap, BoxCollider b, Vec2 bp
        ) {
            double ax = ap.x + a.offset.x;
            double ay = ap.y + a.offset.y;
            double bx = bp.x + b.offset.x;
            double by = bp.y + b.offset.y;
            return bx < ax + a.size.x
                && ax < bx + b.size.x
                && by < ay + a.size.y
                && ay < by + b.size.y;
        }

        public static void resolveCollision(
            Vec2 vel, Vec2 pos, BoxCollider box,
            Vec2 staticPos, BoxCollider staticBox
        ) {
            vel.negate();
            Vec2 corr = vel;
            if(corr.x == 0 && corr.y == 0) {
                Vec2 center = pos.clone()
                    .add(box.offset)
                    .add(box.size.clone().div(2));
                Vec2 compCenter = staticPos.clone()
                    .add(staticBox.offset)
                    .add(staticBox.size.clone().div(2));
                corr = center.sub(compCenter);
            }
            boolean correctOnX = Math.abs(corr.x) > Math.abs(corr.y);
            if(correctOnX) {
                if(corr.x < 0) {
                    pos.x = staticPos.x + staticBox.offset.x 
                        - box.size.x - box.offset.x;
                } else {
                    pos.x = staticPos.x + staticBox.offset.x + staticBox.size.x
                        - box.offset.x;
                }
            } else {
                if(corr.y < 0) {
                    pos.y = staticPos.y + staticBox.offset.y 
                        - box.size.y - box.offset.y;
                } else {
                    pos.y = staticPos.y + staticBox.offset.y + staticBox.size.y
                        - box.offset.y;
                }
            }
        }

        public static void resolveCollision(
            Velocity velA, Position posA, BoxCollider boxA,
            Velocity velB, Position posB, BoxCollider boxB
        ) {
            Vec2 nVelA = velA.value.clone();
            Vec2 nPosA = posA.value.clone();
            BoxCollider.resolveCollision(nVelA, nPosA, boxA, posB.value, boxB);
            Vec2 nVelB = velB.value.clone();
            Vec2 nPosB = posB.value.clone();
            BoxCollider.resolveCollision(nVelB, nPosB, boxB, posA.value, boxA);
            double velLS = velA.value.len() + velB.value.len();
            double velR = velLS == 0
                ? 0.5 
                : velA.value.len() / velLS;
            Vec2 offsetA = nPosA.clone().sub(posA.value).mul(velR);
            Vec2 offsetB = nPosB.clone().sub(posB.value).mul(1 - velR);
            posA.value.add(offsetA);
            posB.value.add(offsetB);
            velA.value = nVelA;
            velB.value = nVelB;
        }
    }


    public Collision() {}
    
    public ArrayList<BoxCollider> boxes = new ArrayList<>();

    public Collision with(BoxCollider... boxes) {
        this.boxes.addAll(List.of(boxes));
        return this;
    }

    private static void handleBoxes(Scene scene) {
        for(Entity entity: scene.allWith(
            Position.class, Velocity.class, Collision.class
        )) {
            Position pos = entity.get(Position.class);
            Velocity vel = entity.get(Velocity.class);
            Collision coll = entity.get(Collision.class);
            for(BoxCollider box: coll.boxes) {
                for(Entity comp: scene.allWith(
                    Position.class, Collision.class
                )) {
                    if(comp == entity) { continue; }
                    Position compPos = comp.get(Position.class);
                    Collision compColl = comp.get(Collision.class);
                    for(BoxCollider compBox: compColl.boxes) {
                        if(!BoxCollider.colliding(
                            box, pos.value, compBox, compPos.value
                        )) { continue; }
                        if(!comp.has(Velocity.class)) {
                            BoxCollider.resolveCollision(
                                vel.value, pos.value, box, 
                                compPos.value, compBox
                            );
                            continue;
                        }
                        Velocity compVel = comp.get(Velocity.class);
                        BoxCollider.resolveCollision(
                            vel, pos, box, compVel, compPos, compBox
                        );
                    } 
                }
            }
        }
    }


    public static void handleAll(Scene scene) {
        Collision.handleBoxes(scene);
    }

}
