
package typesafeschwalbe.luwest.util;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

import typesafeschwalbe.luwest.engine.Entity;
import typesafeschwalbe.luwest.engine.Scene;
import typesafeschwalbe.luwest.math.Vec2;

public final class Sectors {

    private Sectors() {}


    private static class Sector {
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


    public static class Owned {}

    public static class Observer {
        public long sectorSize = 64;
        public long range = 4;
        public BiConsumer<Scene, List<Entity>> deletionHandler = (s, d) -> {};
        public final StaticScene staticScene;
        private long sectorX = 0;
        private long sectorY = 0;

        public Observer(StaticScene staticScene) {
            this.staticScene = staticScene;
        }

        public Observer withSectorSize(long sectorSize) {
            this.sectorSize = sectorSize;
            return this;
        }

        public Observer withRange(long range) {
            this.range = range;
            return this;
        }

        public Observer withDeletionHandler(
            BiConsumer<Scene, List<Entity>> handler
        ) {
            this.deletionHandler = handler;
            return this;
        }

        public long getSectorX(Vec2 pos) {
            return (long) pos.x / this.sectorSize;
        }

        public long getSectorY(Vec2 pos) {
            return (long) pos.y / this.sectorSize;
        }

        private boolean isObserved(Entity entity) {
            Position position = entity.get(Position.class);
            long sectorX = this.getSectorX(position.value);
            long sectorY = this.getSectorY(position.value);
            long distance = Math.abs(sectorX - this.sectorX)
                + Math.abs(sectorY - this.sectorY);
            return distance <= this.range;
        }

        private void deleteUnobserved(Scene scene) {
            LinkedList<Entity> deleted = new LinkedList<>();
            for(Entity entity: scene.allWith(Owned.class, Position.class)) {
                if(this.isObserved(entity)) { continue; }
                deleted.add(entity);
                scene.remove(entity);
            }
            this.deletionHandler.accept(scene, deleted);
        }

        private void spawnObserved(Scene scene) {
            HashSet<Sector> seen = new HashSet<>();
            for(Entity entity: scene.allWith(Owned.class, Position.class)) {
                Position position = entity.get(Position.class);
                seen.add(new Sector(
                    this.getSectorX(position.value), 
                    this.getSectorY(position.value)
                ));
            }
            Sector sector = new Sector(0, 0);
            for(
                sector.x = this.sectorX - this.range; 
                sector.x <= this.sectorX + range; 
                sector.x += 1
            ) {
                for(
                    sector.y = this.sectorY - this.range;
                    sector.y <= this.sectorY + range;
                    sector.y += 1
                ) {
                    if(seen.contains(sector)) { continue; }
                    this.staticScene
                        .deserializeSector(sector.x, sector.y, scene);
                }
            }
        }
    }


    public static void manageAll(Scene scene) {
        for(Entity entity: scene.allWith(Observer.class, Position.class)) {
            Observer observer = entity.get(Observer.class);
            Position position = entity.get(Position.class);
            observer.sectorX = observer.getSectorX(position.value);
            observer.sectorY = observer.getSectorY(position.value);
            observer.deleteUnobserved(scene);
            observer.spawnObserved(scene);
        }
    }

}
