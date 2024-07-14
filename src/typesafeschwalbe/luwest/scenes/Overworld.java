
package typesafeschwalbe.luwest.scenes;

import java.awt.AlphaComposite;
import java.awt.Composite;

import com.google.gson.JsonParser;

import typesafeschwalbe.luwest.engine.*;
import typesafeschwalbe.luwest.util.Camera;
import typesafeschwalbe.luwest.util.SpriteRenderer;
import typesafeschwalbe.luwest.util.Serialization;

public class Overworld {

    public static void renderInfiniteWater(Scene scene) {
        for(Entity camera: scene.allWith(Camera.Buffer.class)) {
            Camera.Buffer buffer = camera.get(Camera.Buffer.class);
            buffer.world.add(0.0, g -> {
                g.setColor(new java.awt.Color(126, 196, 193));
                g.fillRect(
                    0, 0, 
                    buffer.worldBuff.getWidth(), buffer.worldBuff.getHeight()
                );
                Composite prev = g.getComposite();
                g.setComposite(AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER, 0.5f
                ));
                g.drawImage(buffer.reflectBuff, 0, 0, null);
                g.setComposite(prev);
            });
        }
    }

    public static Scene createScene() {
        return new Scene()
            .with(
                Camera.create(20.0),
                Serialization.deserialize(
                    JsonParser.parseString("""
                        {
                            "type": "res/props/oak_tree_1.json",
                            "at": [0.0, 0.0]
                        }     
                    """).getAsJsonObject(), 
                    Resource.EMBEDDED
                )
            )
            .with(
                Camera::computeOffsets,

                SpriteRenderer::renderReflections,
                Camera::renderReflections,

                SpriteRenderer::renderAll,
                Overworld::renderInfiniteWater,
                Camera::renderAll,
                
                Camera::showBuffers
            );
    }

}   