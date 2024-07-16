package typesafeschwalbe.luwest.scenes;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.util.HashMap;
import java.util.Optional;

import com.google.gson.JsonObject;

import typesafeschwalbe.luwest.engine.*;
import typesafeschwalbe.luwest.math.Vec2;
import typesafeschwalbe.luwest.util.Camera;
import typesafeschwalbe.luwest.util.Position;
import typesafeschwalbe.luwest.util.Serialization.Serializer;

public final class Lake {
    
    private Lake() {}


    public static class WaterRenderer {
        public Vec2 size;

        public WaterRenderer(Vec2 size) {
            this.size = size;
        }
    }

    public static void renderAll(Scene scene) {
        for(Entity camera: scene.allWith(
            Camera.Buffer.class, Camera.Conversion.class
        )) {
            Camera.Conversion conv = camera.get(Camera.Conversion.class);
            Camera.Buffer buffer = camera.get(Camera.Buffer.class);
            for(Entity lake: scene.allWith(
                WaterRenderer.class, Position.class
            )) {
                Position position = lake.get(Position.class);
                WaterRenderer renderer = lake.get(WaterRenderer.class);
                Vec2 sPos = conv.posOnScreen(position.value.clone());
                Vec2 sSize = conv.sizeOnScreen(renderer.size.clone());
                buffer.world.add(sPos.y, g -> {
                    g.setColor(new java.awt.Color(126, 196, 193));
                    g.fillRect(
                        (int) sPos.x, (int) sPos.y,
                        (int) sSize.x, (int) sSize.y
                    );
                    Composite prev = g.getComposite();
                    g.setComposite(AlphaComposite.getInstance(
                        AlphaComposite.SRC_OVER, 0.5f
                    ));
                    g.drawImage(
                        buffer.reflectBuff, 
                        (int) sPos.x, (int) sPos.y,
                        (int) (sPos.x + sSize.x), (int) (sPos.y + sSize.y),
                        (int) sPos.x, (int) sPos.y,
                        (int) (sPos.x + sSize.x), (int) (sPos.y + sSize.y),
                        null
                    );
                    g.setComposite(prev);
                });
            }
        }
    }


    public static class LakeSerializer implements Serializer {
        static final HashMap<JsonObject, WaterRenderer> RENDERER_CACHE
            = new HashMap<>();

        @Override
        public Entity deserialize(
            Optional<JsonObject> instance, JsonObject type, 
            Resource.Origin origin
        ) {
            Vec2 at;
            if(instance.isPresent()) {
                at = new Vec2(instance.get().get("at").getAsJsonArray());
            } else {
                at = new Vec2();
            }
            Vec2 size = new Vec2(type.get("size").getAsJsonArray());
            WaterRenderer renderer = LakeSerializer.RENDERER_CACHE.get(type);
            if(renderer == null) {
                renderer = new WaterRenderer(size);
                LakeSerializer.RENDERER_CACHE.put(type, renderer);
            }
            return new Entity()
                .with(Position.class, new Position(at))
                .with(WaterRenderer.class, renderer);
        }

        @Override
        public JsonObject serialize(Entity entity) {
            Position position = entity.get(Position.class);
            JsonObject instance = new JsonObject();
            instance.add("at", position.value.asJsonArray());
            return instance;
        }
    }

}
