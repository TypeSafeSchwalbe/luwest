package typesafeschwalbe.luwest.engine;

import java.util.HashMap;

public class Entity {

    private final HashMap<Class<?>, Object> components;

    public Entity() {
        this.components = new HashMap<>();
    }

    private Entity(HashMap<Class<?>, Object> components) {
        this.components = components;
    }
    
    public Entity clone() {
        return new Entity(this.components);
    }

    public <T> Entity with(Class<T> component, T value) {
        this.components.put(component, value);
        return this;
    }

    public boolean has(Class<?> component) {
        return this.components.containsKey(component);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> component) {
        return (T) this.components.get(component);
    }

}
