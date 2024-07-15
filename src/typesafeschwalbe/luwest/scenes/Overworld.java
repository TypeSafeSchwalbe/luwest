
package typesafeschwalbe.luwest.scenes;

import java.awt.AlphaComposite;
import java.awt.Composite;

import typesafeschwalbe.luwest.engine.*;
import typesafeschwalbe.luwest.util.Camera;
import typesafeschwalbe.luwest.util.Sectors;
import typesafeschwalbe.luwest.util.SpriteRenderer;
import typesafeschwalbe.luwest.util.StaticScene;

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
        StaticScene scene = new StaticScene(
            "res/scenes/overworld.json", Resource.EMBEDDED
        );
        return new Scene()
            .with(
                Camera.create(20.0)
                    .with(Sectors.Observer.class, new Sectors.Observer(scene))
            )
            .with(
                Sectors::manageAll,
                Camera::computeOffsets,

                SpriteRenderer::renderReflections,
                Camera::renderReflections,

                scene::renderBackground,
                SpriteRenderer::renderAll,
                //Overworld::renderInfiniteWater,
                Camera::renderAll,
                
                Camera::showBuffers
            );
    }

}   