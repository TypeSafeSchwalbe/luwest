
package typesafeschwalbe.luwest.util;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import typesafeschwalbe.luwest.engine.Entity;
import typesafeschwalbe.luwest.engine.Scene;
import typesafeschwalbe.luwest.math.Vec2;

public final class Sectors {

    private Sectors() {}


    public static class Sector {
        public long x;
        public long y;

        public Sector(long x, long y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object otherRaw) {
            if(!(otherRaw instanceof Sector)) { return false; }
            Sector other = (Sector) otherRaw;
            return this.x == other.x
                && this.y == other.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.x, this.y);
        }
    }


    @FunctionalInterface
    public interface DeletionHandler {
        void handle(Scene scene, List<Entity> deleted);
    }


    public static class Owned {}

    public static class Observer {
        public long range = 4;
        public DeletionHandler deletionHandler = (s, d) -> {};
        public final StaticScene staticScene;
        private Sector sector = new Sector(0, 0);

        public Observer(StaticScene staticScene) {
            this.staticScene = staticScene;
        }

        public Observer withRange(long range) {
            this.range = range;
            return this;
        }

        public Observer withDeletionHandler(DeletionHandler handler) {
            this.deletionHandler = handler;
            return this;
        }

        public long asSectorX(Vec2 pos) {
            return (long) pos.x / this.staticScene.sectorSize;
        }

        public long asSectorY(Vec2 pos) {
            return (long) pos.y / this.staticScene.sectorSize;
        }

        public Sector asSector(Vec2 pos) {
            return new Sector(this.asSectorX(pos), this.asSectorY(pos));
        }

        private boolean isObserved(Entity entity) {
            Position position = entity.get(Position.class);
            long sectorX = this.asSectorX(position.value);
            long sectorY = this.asSectorY(position.value);
            long distance = Math.abs(sectorX - this.sector.x)
                + Math.abs(sectorY - this.sector.y);
            return distance <= this.range;
        }

        private void deleteUnobserved(Scene scene) {
            LinkedList<Entity> deleted = new LinkedList<>();
            for(Entity entity: scene.allWith(Owned.class, Position.class)) {
                if(this.isObserved(entity)) { continue; }
                deleted.add(entity);
                scene.remove(entity);
            }
            if(deleted.size() > 0) {
                this.deletionHandler.handle(scene, deleted);
            }
        }

        private void spawnObserved(Scene scene) {
            HashSet<Sector> seen = new HashSet<>();
            for(Entity entity: scene.allWith(Owned.class, Position.class)) {
                Position position = entity.get(Position.class);
                seen.add(new Sector(
                    this.asSectorX(position.value), 
                    this.asSectorY(position.value)
                ));
            }
            Sector checked = new Sector(0, 0);
            long r = this.range / 2;
            for(
                checked.x = this.sector.x - r; 
                checked.x <= this.sector.x + r; 
                checked.x += 1
            ) {
                for(
                    checked.y = this.sector.y - r;
                    checked.y <= this.sector.y + r;
                    checked.y += 1
                ) {
                    if(seen.contains(checked)) { continue; }
                    this.staticScene
                        .deserializeSector(checked.x, checked.y, scene);
                }
            }
        }
    }


    public static void manageAll(Scene scene) {
        for(Entity entity: scene.allWith(Observer.class, Position.class)) {
            Observer observer = entity.get(Observer.class);
            Position position = entity.get(Position.class);
            observer.sector.x = observer.asSectorX(position.value);
            observer.sector.y = observer.asSectorY(position.value);
            observer.deleteUnobserved(scene);
            observer.spawnObserved(scene);
        }
    }

}
