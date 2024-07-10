
package typesafeschwalbe.luwest.engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class Scene {

    @FunctionalInterface
    public interface System {
        void run(Scene scene);
    }

    private ArrayList<Entity> entities = new ArrayList<>();
    private ArrayList<System> systems = new ArrayList<>();

    public Scene() {}

    public Scene(String terrain) {
        // TODO!
    }

    public Scene with(Entity... entity) {
        this.entities.addAll(List.of(entity));
        return this;
    }

    public Scene with(System... systems) {
        this.systems.addAll(List.of(systems));
        return this;
    }

    private static class EntitiesWith implements Iterator<Entity> {
        final Class<?>[] with;
        final Scene in;
        int idx = 0;
        Entity current = null;

        EntitiesWith(Class<?>[] component, Scene scene) {
            this.with = component;
            this.in = scene;
            this.proceed();
        }

        void proceed() {
            while(true) {
                if(this.idx >= this.in.entities.size()) {
                    this.current = null;
                    break;
                }
                this.current = this.in.entities.get(this.idx);
                this.idx += 1;
                if(Arrays.stream(this.with).allMatch(this.current::has)) {
                    break;
                }
            }
        }

        @Override
        public boolean hasNext() { return this.current != null; }

        @Override
        public Entity next() {
            Entity current = this.current;
            this.proceed();
            return current;
        }
    }

    public Iterable<Entity> allWith(Class<?>... components) {
        return () -> new EntitiesWith(components, this);
    }

    public void runSystems() {
        for(int sysIdx = 0; sysIdx < this.systems.size(); sysIdx += 1) {
            this.systems.get(sysIdx).run(this);
        }
    }

}