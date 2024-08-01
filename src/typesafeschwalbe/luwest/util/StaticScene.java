
package typesafeschwalbe.luwest.util;

import java.awt.Color;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import typesafeschwalbe.luwest.engine.*;

public class StaticScene {

    public final JsonObject json;
    public final Resource.Origin origin;
    public final long sectorSize;

    public StaticScene(String file, Resource.Origin origin) {
        this.json = Resource.json(file, origin).get().getAsJsonObject();
        this.origin = origin;
        this.sectorSize = this.json.get("sector_size").getAsLong();
    }

    public void renderBackground(Scene scene) {
        Color background = Color.decode(json.get("background").getAsString());
        for(Entity camera: scene.allWith(Camera.Buffer.class)) {
            Camera.Buffer buffer = camera.get(Camera.Buffer.class);
            buffer.world.add(Double.NEGATIVE_INFINITY, g -> {
                g.setColor(background);
                g.fillRect(
                    0, 0, Engine.window().width(), Engine.window().height()
                );
            });
        }
    }

    public JsonArray sectorJson(long sectorX, long sectorY) {
        String sectorId = Long.valueOf(sectorX) + "|" + Long.valueOf(sectorY);
        JsonObject sectors = this.json.get("sectors").getAsJsonObject();
        if(!sectors.has(sectorId)) { return null; }
        return sectors.get(sectorId).getAsJsonArray();
    }

    public void deserializeSector(long sectorX, long sectorY, Scene scene) {
        JsonArray sector = this.sectorJson(sectorX, sectorY);
        if(sector == null) { return; }
        for(JsonElement instanceElem: sector) {
            JsonObject instance = instanceElem.getAsJsonObject();
            Entity entity = Serialization
                .deserializeInstance(instance, this.origin);
            scene.with(entity.with(Sectors.Owned.class, new Sectors.Owned()));
        }
    }

    public void serializeSector(
        long sectorX, long sectorY, Sectors.Observer observer, Scene scene
    ) {
        JsonArray sector = new JsonArray();
        for(Entity entity: scene.allWith(Position.class, Sectors.Owned.class)) {
            Position position = entity.get(Position.class);
            if(observer.asSectorX(position.value) != sectorX) { continue; }
            if(observer.asSectorY(position.value) != sectorY) { continue; }
            JsonObject instance = Serialization.serializeInstance(entity);
            sector.add(instance);
        }
        String sectorId = Long.valueOf(sectorX) + "|" + Long.valueOf(sectorY);
        JsonObject sectors = this.json.get("sectors").getAsJsonObject();
        if(sector.size() > 0) {
            sectors.add(sectorId, sector);
        } else {
            sectors.remove(sectorId);
        }
    }

    public String serialize() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(this.json);
    }

}