
package typesafeschwalbe.luwest.scenes;

import typesafeschwalbe.luwest.engine.*;
import typesafeschwalbe.luwest.util.Camera;
import typesafeschwalbe.luwest.util.Sectors;
import typesafeschwalbe.luwest.util.Serialization;
import typesafeschwalbe.luwest.util.SpriteRenderer;
import typesafeschwalbe.luwest.util.StaticScene;

public class Overworld {

    public static Scene createScene() {
        Serialization.define("lake", new Lake.LakeSerializer());
        StaticScene scene = new StaticScene(
            "res/scenes/overworld.json", Resource.EMBEDDED
        );
        return new Scene()
            .with(Camera.create(20.0).with(
                Sectors.Observer.class, new Sectors.Observer(scene, 4)
            ))
            .with(
                Sectors::manageAll,
                Camera::computeOffsets,

                SpriteRenderer::renderReflections,
                Camera::renderReflections,

                scene::renderBackground,
                SpriteRenderer::renderAll,
                Lake::renderAll,
                Camera::renderAll,
                
                Camera::showBuffers
            );
    }

}   