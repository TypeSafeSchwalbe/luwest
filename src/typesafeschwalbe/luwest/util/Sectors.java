
package typesafeschwalbe.luwest.util;

import java.util.HashSet;
import java.util.Objects;
import java.util.stream.LongStream;

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


    public static class Owned {}

    public static class Observer {
        public final long radius;
        public final StaticScene staticScene;
        private final Sector sector = new Sector(0, 0);

        public Observer(StaticScene staticScene, long radius) {
            this.staticScene = staticScene;
            this.radius = radius;
        }

        public long asSectorX(Vec2 pos) {
            return (long) Math.floor(pos.x / this.staticScene.sectorSize);
        }

        public long asSectorY(Vec2 pos) {
            return (long) Math.floor(pos.y / this.staticScene.sectorSize);
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
            return distance / 2 <= this.radius;
        }

        public Iterable<Sector> observedSectors() {
            long d = this.radius * 2 + 1;
            return () -> LongStream.rangeClosed(0, d * d)
                .mapToObj(i -> new Sector(
                    i % d - this.radius + this.sector.x, 
                    i / d - this.radius + this.sector.y
                ))
                .iterator();
        }

        private void deleteUnobserved(Scene scene) {
            for(Entity entity: scene.allWith(Owned.class, Position.class)) {
                if(this.isObserved(entity)) { continue; }
                scene.remove(entity);
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
            for(Sector checked: this.observedSectors()) {
                if(seen.contains(checked)) { continue; }
                this.staticScene
                    .deserializeSector(checked.x, checked.y, scene);
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
