
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

    public static void renderInfiniteWater(Scene scene) {
        for(Entity camera: scene.allWith(Camera.Buffer.class)) {
            Camera.Buffer buffer = camera.get(Camera.Buffer.class);
            buffer.world.add(0.0, g -> {
                g.drawImage(buffer.reflectBuff, 0, 0, null);
            });
        }
    }

    public static Entity createTree(Vec2 position) {
        return new Entity()
            .with(Position.class, new Position(position))
            .with(SpriteRenderer.class, new SpriteRenderer(
                OAK_TREE_1, new Vec2(31, 79), new Vec2(4, 5)
            ));
    }

    public static Scene createScene() {
        return new Scene()
            .with(
                Camera.create(20.0),
                Overworld.createTree(new Vec2(-2.0, -1.0)),
                Overworld.createTree(new Vec2( 0.0,  0.0)),
                Overworld.createTree(new Vec2( 2.0,  1.0))
            )
            .with(
                Camera::computeOffsets,

                SpriteRenderer::renderReflections,
                Camera::renderReflections,

                SpriteRenderer::renderAll,
                Overworld::renderInfiniteWater,
                Camera::renderAll,
                
                Camera::showBuffers
            )
            .with(OAK_TREE_1);
    }

}   