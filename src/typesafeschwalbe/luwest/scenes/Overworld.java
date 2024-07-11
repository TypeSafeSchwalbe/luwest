
package typesafeschwalbe.luwest.scenes;

import java.awt.image.BufferedImage;

import typesafeschwalbe.luwest.engine.*;
import typesafeschwalbe.luwest.math.Vec2;
import typesafeschwalbe.luwest.util.Camera;
import typesafeschwalbe.luwest.util.Position;
import typesafeschwalbe.luwest.util.SpriteRenderer;

public class Overworld {

    public static Resource<BufferedImage> OAK_TREE_1 
        = Resource.embeddedImage("res/textures/oak_tree_1.png");

    public static Scene createScene() {
        return new Scene()
            .with(new Entity()
                .with(Position.class, new Position())
                .with(SpriteRenderer.class, new SpriteRenderer(
                    OAK_TREE_1, new Vec2(31, 79), new Vec2(4, 5)
                ))
            )
            .with(Camera.create(10.0))
            .with(Camera::resizeBuffers, Camera::computeOffsets)
            .with(SpriteRenderer::renderAll)
            .with(Camera::showBuffers)
            .with(OAK_TREE_1);
    }

}