
package typesafeschwalbe.luwest.util;

import java.util.HashMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import typesafeschwalbe.luwest.engine.Entity;
import typesafeschwalbe.luwest.engine.Resource;
import typesafeschwalbe.luwest.engine.Resource.Origin;
import typesafeschwalbe.luwest.math.Vec2;

public class Serialization {

    private Serialization() {}


    public interface Serializer {
        Entity deserialize(
            JsonObject instance, JsonObject type, Origin origin
        );
        JsonObject serialize(Entity entity);
    }


    public static class Serializable {
        public final String type;
        public final Serializer serializer;

        public Serializable(String type, Serializer serializer) {
            this.type = type;
            this.serializer = serializer;
        }
    }


    private static final HashMap<String, Serializer> SERIALIZERS 
        = new HashMap<>();
    
    public static void define(String name, Serializer serializer) {
        Serialization.SERIALIZERS.put(name, serializer);
    }

    public static Entity deserialize(JsonObject instance, Origin origin) {
        String typePath = instance.get("type").getAsString();
        JsonObject type = Resource.json(typePath, origin).get();
        String serializerName = type.get("serializer").getAsString();
        Serializer serializer = Serialization.SERIALIZERS.get(serializerName);
        return serializer.deserialize(instance, type, origin)
            .with(Serializable.class, new Serializable(typePath, serializer));
    }

    public static JsonObject serialize(Entity entity) {
        Serializable serialization = entity.get(Serializable.class);
        JsonObject instance = serialization.serializer.serialize(entity);
        instance.add("type", new JsonPrimitive(serialization.type));
        return instance;
    }


    public static class PropSerializer implements Serializer {
        static final HashMap<JsonObject, SpriteRenderer> RENDERER_CACHE
            = new HashMap<>();

        @Override
        public Entity deserialize(
            JsonObject instance, JsonObject type, Origin origin
        ) {
            JsonArray at = instance.get("at").getAsJsonArray();
            String spritePath = type.get("sprite").getAsString();
            JsonArray anchor = type.get("anchor").getAsJsonArray();
            JsonArray size = type.get("size").getAsJsonArray();
            SpriteRenderer renderer = PropSerializer.RENDERER_CACHE.get(type);
            if(renderer == null) {
                renderer = new SpriteRenderer(
                    Resource.image(spritePath, origin), 
                    new Vec2(anchor), 
                    new Vec2(size)
                );
                PropSerializer.RENDERER_CACHE.put(type, renderer);
            }
            return new Entity()
                .with(Position.class, new Position(new Vec2(at)))
                .with(SpriteRenderer.class, renderer);
        }

        @Override
        public JsonObject serialize(Entity entity) {
            Position position = entity.get(Position.class);
            JsonObject instance = new JsonObject();
            instance.add("at", position.value.asJsonArray());
            return instance;
        }
    }

    static {
        Serialization.define("prop", new PropSerializer());    
    }

}
