
package typesafeschwalbe.luwest.util;

import java.util.HashMap;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import typesafeschwalbe.luwest.engine.Entity;

public class Serialization {

    private Serialization() {}


    @FunctionalInterface
    public interface StringReader {
        String read(String path);
    }


    public interface Serializer {
        Entity deserialize(JsonObject instance, JsonObject type);
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

    private static final HashMap<String, JsonObject> CACHED_TYPES
        = new HashMap<>();

    private static JsonObject readType(String path, StringReader reader) {
        JsonObject cached = Serialization.CACHED_TYPES.get(path);
        if(cached != null) { return cached; }
        String file = reader.read(path);
        JsonObject type = JsonParser.parseString(file).getAsJsonObject();
        Serialization.CACHED_TYPES.put(path, type);
        return type;
    }

    public static Entity deserialize(JsonObject instance, StringReader reader) {
        String typePath = instance.get("type").getAsString();
        JsonObject type = Serialization.readType(typePath, reader);
        String serializerName = type.get("serializer").getAsString();
        Serializer serializer = Serialization.SERIALIZERS.get(serializerName);
        return serializer.deserialize(instance, type)
            .with(Serializable.class, new Serializable(typePath, serializer));
    }

    public static JsonObject serialize(Entity entity) {
        Serializable serialization = entity.get(Serializable.class);
        JsonObject instance = serialization.serializer.serialize(entity);
        instance.add("type", new JsonPrimitive(serialization.type));
        return instance;
    }

}
