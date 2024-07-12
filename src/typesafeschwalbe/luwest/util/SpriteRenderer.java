
package typesafeschwalbe.luwest.util;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import typesafeschwalbe.luwest.engine.*;
import typesafeschwalbe.luwest.math.Vec2;

public class SpriteRenderer {

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

    public static void renderReflections(Scene scene) {
        for(Entity thing: scene.allWith(SpriteRenderer.class, Position.class)) {
            SpriteRenderer r = thing.get(SpriteRenderer.class);
            Position worldPos = thing.get(Position.class);
            for(Entity camera: scene.allWith(
                Camera.Conversion.class, Camera.Buffer.class
            )) {
                Camera.Conversion conv = camera.get(Camera.Conversion.class);
                Camera.Buffer buffer = camera.get(Camera.Buffer.class);
                Vec2 sSize = conv.sizeOnScreen(r.size.clone());
                Vec2 anchorOffset = r.anchor.clone()
                    .div(r.sprite.get().getWidth(), r.sprite.get().getHeight())
                    .mul(sSize);
                Vec2 anchorSPos = conv.posOnScreen(worldPos.value.clone());
                double sPosX = anchorSPos.x - anchorOffset.x;
                double sPosY = anchorSPos.y + Math.max(anchorOffset.y, sSize.y);
                buffer.reflect.add(anchorSPos.y, g -> {
                    g.drawImage(
                        r.sprite.get(),
                        (int) sPosX, (int) sPosY, 
                        (int) sSize.x, (int) (-sSize.y), 
                        null
                    );
                });
            }
        }
    }

    public static void renderAll(Scene scene) {
        for(Entity thing: scene.allWith(SpriteRenderer.class, Position.class)) {
            SpriteRenderer r = thing.get(SpriteRenderer.class);
            Position worldPos = thing.get(Position.class);
            for(Entity camera: scene.allWith(
                Camera.Conversion.class, Camera.Buffer.class
            )) {
                Camera.Conversion conv = camera.get(Camera.Conversion.class);
                Camera.Buffer buffer = camera.get(Camera.Buffer.class);
                Vec2 sSize = conv.sizeOnScreen(r.size.clone());
                Vec2 anchorOffset = r.anchor.clone()
                    .div(r.sprite.get().getWidth(), r.sprite.get().getHeight())
                    .mul(sSize);
                Vec2 anchorSPos = conv.posOnScreen(worldPos.value.clone());
                Vec2 sPos = anchorSPos.clone().sub(anchorOffset);
                buffer.world.add(anchorSPos.y, g -> {
                    g.drawImage(
                        r.sprite.get(),
                        (int) sPos.x, (int) sPos.y, 
                        (int) sSize.x, (int) sSize.y, 
                        null
                    );
                });
            }
        }
    }

}
