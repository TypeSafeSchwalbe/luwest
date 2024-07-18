
package typesafeschwalbe.luwest.util;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Optional;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import typesafeschwalbe.luwest.engine.Entity;
import typesafeschwalbe.luwest.engine.Resource;
import typesafeschwalbe.luwest.engine.Resource.Origin;
import typesafeschwalbe.luwest.math.Vec2;

public final class Serialization {

    private Serialization() {}


    public interface Serializer {
        Entity deserialize(
            Optional<JsonObject> instance, JsonObject type, Origin origin
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

    public static Entity createInstance(String typePath, Origin origin) {
        JsonObject type = Resource.json(typePath, origin).get();
        String serializerName = type.get("serializer").getAsString();
        Serializer serializer = Serialization.SERIALIZERS.get(serializerName);
        return serializer.deserialize(Optional.empty(), type, origin)
            .with(Serializable.class, new Serializable(typePath, serializer));
    }

    public static Entity deserializeInstance(
        JsonObject instance, Origin origin
    ) {
        String typePath = instance.get("type").getAsString();
        JsonObject type = Resource.json(typePath, origin).get();
        String serializerName = type.get("serializer").getAsString();
        Serializer serializer = Serialization.SERIALIZERS.get(serializerName);
        if(serializer == null) {
            throw new RuntimeException(
                "'" + serializerName + "' is not a registered serializer!"
            );
        }
        return serializer.deserialize(Optional.of(instance), type, origin)
            .with(Serializable.class, new Serializable(typePath, serializer));
    }

    public static JsonObject serializeInstance(Entity entity) {
        Serializable serialization = entity.get(Serializable.class);
        JsonObject instance = serialization.serializer.serialize(entity);
        instance.add("type", new JsonPrimitive(serialization.type));
        return instance;
    }


    public static class PropSerializer implements Serializer {
        static final HashMap<JsonObject, SpriteRenderer> RENDERER_CACHE
            = new HashMap<>();
        static final HashMap<JsonObject, Collision> COLLISION_CACHE
            = new HashMap<>();

        @Override
        public Entity deserialize(
            Optional<JsonObject> instance, JsonObject type, Origin origin
        ) {
            Vec2 at;
            if(instance.isPresent()) {
                at = new Vec2(instance.get().get("at").getAsJsonArray());
            } else {
                at = new Vec2();
            }
            String spritePath = type.get("sprite").getAsString();
            Resource<BufferedImage> sprite = Resource.image(spritePath, origin);
            Vec2 anchor = new Vec2(type.get("anchor").getAsJsonArray());
            Vec2 size = new Vec2(type.get("size").getAsJsonArray());
            SpriteRenderer renderer = PropSerializer.RENDERER_CACHE.get(type);
            if(renderer == null) {
                renderer = new SpriteRenderer(sprite, anchor, size);
                PropSerializer.RENDERER_CACHE.put(type, renderer);
            }
            Collision collision = PropSerializer.COLLISION_CACHE.get(type);
            if(collision == null) {
                collision = new Collision();
                JsonArray colliders = type.get("colliders").getAsJsonArray();
                for(JsonElement boxElem: colliders) {
                    JsonObject box = boxElem.getAsJsonObject();
                    Vec2 bOffset = new Vec2(box.get("offset").getAsJsonArray())
                        .div(sprite.get().getWidth(), sprite.get().getHeight())
                        .mul(size);
                    Vec2 bSize = new Vec2(box.get("size").getAsJsonArray())
                        .div(sprite.get().getWidth(), sprite.get().getHeight())
                        .mul(size);
                    collision.with(new Collision.BoxCollider(bOffset, bSize));
                }
            }
            return new Entity()
                .with(Position.class, new Position(at))
                .with(SpriteRenderer.class, renderer)
                .with(Collision.class, collision);
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
