
package typesafeschwalbe.luwest.util;

import java.awt.Color;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import typesafeschwalbe.luwest.engine.*;

public class StaticScene {

    public final JsonObject json;
    public final Resource.Origin origin;
    public final long sectorSize;

    public StaticScene(String file, Resource.Origin origin) {
        this.json = Resource.json(file, origin).get();
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

    public void deserializeSector(long sectorX, long sectorY, Scene scene) {
        String sectorId = Long.valueOf(sectorX) + "|" + Long.valueOf(sectorY);
        JsonObject sectors = this.json.get("sectors").getAsJsonObject();
        if(!sectors.has(sectorId)) { return; }
        JsonObject sector = sectors.get(sectorId).getAsJsonObject();
        for(JsonElement instanceElem: sector.get("entities").getAsJsonArray()) {
            JsonObject instance = instanceElem.getAsJsonObject();
            Entity entity = Serialization.deserialize(instance, this.origin);
            scene.with(entity.with(Sectors.Owned.class, new Sectors.Owned()));
        }
    }

}