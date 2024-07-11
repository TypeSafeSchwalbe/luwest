
package typesafeschwalbe.luwest.scenes;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import typesafeschwalbe.luwest.engine.*;
import typesafeschwalbe.luwest.math.Vec2;
import typesafeschwalbe.luwest.util.Camera;
import typesafeschwalbe.luwest.util.Position;

public class Overworld {

    public static Resource<BufferedImage> OAK_TREE_1 
        = Resource.embeddedImage("res/textures/oak_tree_1.png");

    public static class SpriteRenderer {
        public Resource<BufferedImage> sprite;
        public Vec2 anchor;
        public Vec2 size;

        public SpriteRenderer(
            Resource<BufferedImage> sprite, Vec2 anchor, Vec2 size
        ) {
            this.sprite = sprite;
            this.anchor = anchor;
            this.size = size;
        }
    }

    public static void renderSprites(Scene scene) {
        for(Entity thing: scene.allWith(
            SpriteRenderer.class, Position.class
        )) {
            SpriteRenderer sprite = thing.get(SpriteRenderer.class);
            Position pos = thing.get(Position.class);
            for(Entity camera: scene.allWith(
                Camera.Conversion.class, Camera.Buffer.class
            )) {
                Camera.Conversion conv = camera.get(Camera.Conversion.class);
                Camera.Buffer buffer = camera.get(Camera.Buffer.class);
                Graphics2D g = buffer.world.createGraphics();
                Vec2 spriteSize = conv.sizeOnScreen(sprite.size.clone());
                Vec2 anchorOffset = sprite.anchor.clone()
                    .div(
                        sprite.sprite.get().getWidth(), 
                        sprite.sprite.get().getHeight()
                    )
                    .mul(spriteSize);
                Vec2 spritePos = conv.posOnScreen(pos.value.clone())
                    .sub(anchorOffset);
                g.drawImage(
                    OAK_TREE_1.get(),
                    (int) spritePos.x, (int) spritePos.y, 
                    (int) spriteSize.x, (int) spriteSize.y,
                    null
                );
                g.dispose();
            }
        }
    }

    public static Scene createScene() {
        return new Scene("overworld.json")
            .with(new Entity()
                .with(Position.class, new Position())
                .with(SpriteRenderer.class, new SpriteRenderer(
                    OAK_TREE_1, new Vec2(31, 79), new Vec2(4, 5)
                ))
            )
            .with(Camera.create(10.0))
            .with(Camera::resizeBuffers, Camera::computeOffsets)
            .with(Overworld::renderSprites)
            .with(Camera::showBuffers)
            .with(OAK_TREE_1);
    }

}