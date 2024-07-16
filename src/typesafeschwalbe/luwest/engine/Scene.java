
package typesafeschwalbe.luwest.engine;

import java.util.*;

public class Scene {

    @FunctionalInterface
    public interface System {
        void run(Scene scene);
    }

    private static class SystemState {
        System impl;
        Optional<String> tag;
        boolean enabled;
    }

    private LinkedList<Entity> entities = new LinkedList<>();
    private HashSet<Entity> removedEntities = new HashSet<>();
    private ArrayList<SystemState> systems = new ArrayList<>();
    private LinkedList<Runnable> frameEndHandlers = new LinkedList<>();

    public Scene() {}

    public void afterFrame(Runnable handler) {
        this.frameEndHandlers.add(handler);
    }

    public Scene with(Entity... entities) {
        this.entities.addAll(List.of(entities));
        return this;
    }

    public void remove(Entity... entities) {
        this.removedEntities.addAll(List.of(entities));
    }

    public Scene with(System... systems) {
        return this.with(null, systems);
    }

    public Scene with(String tag, System... systems) {
        for(System added: systems) {
            SystemState state = new SystemState();
            state.enabled = true;
            state.tag = Optional.ofNullable(tag);
            state.impl = added;
            this.systems.add(state);
        }
        return this;
    }

    public void setEnabled(String tag, boolean enabled) {
        for(SystemState system: this.systems) {
            if(system.tag.isEmpty()) { continue; }
            if(!system.tag.get().equals(tag)) { continue; }
            system.enabled = enabled;
        }
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

    void runSystems() {
        for(int sysIdx = 0; sysIdx < this.systems.size(); sysIdx += 1) {
            SystemState system = this.systems.get(sysIdx);
            if(!system.enabled) { continue; }
            system.impl.run(this);
        }
        this.entities.removeAll(this.removedEntities);
        this.removedEntities.clear();
        for(Runnable handler: this.frameEndHandlers) {
            handler.run();
        }
        this.frameEndHandlers.clear();
    }

}