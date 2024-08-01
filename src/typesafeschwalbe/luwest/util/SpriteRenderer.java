
package typesafeschwalbe.luwest.util;

import java.awt.image.BufferedImage;

import typesafeschwalbe.luwest.engine.*;
import typesafeschwalbe.luwest.math.Vec2;

public class SpriteRenderer {

    public Resource<BufferedImage> sprite;
    public Vec2 srcOffset;
    public Vec2 srcSize;
    public Vec2 anchor;
    public Vec2 size;

    public SpriteRenderer(
        Resource<BufferedImage> sprite, 
        Vec2 srcOffset, Vec2 srcSize,
        Vec2 anchor, Vec2 size
    ) {
        this.sprite = sprite;
        this.srcOffset = srcOffset;
        this.srcSize = srcSize;
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
                int sWidth = (int) Math.ceil(sSize.x);
                int sHeight = (int) Math.ceil(sSize.y);
                Vec2 anchorOffset = r.anchor.clone()
                    .sub(r.srcOffset)
                    .div(r.srcSize)
                    .mul(sSize);
                Vec2 anchorSPos = conv.posOnScreen(worldPos.value.clone());
                double sPosX = anchorSPos.x - anchorOffset.x;
                double sPosY = anchorSPos.y + Math.max(anchorOffset.y, sSize.y);
                buffer.reflect.add(anchorSPos.y, g -> g.drawImage(
                    r.sprite.get(),
                    (int) sPosX, (int) sPosY, 
                    (int) sPosX + sWidth, (int) sPosY - sHeight,
                    (int) r.srcOffset.x, (int) r.srcOffset.y,
                    (int) (r.srcOffset.x + r.srcSize.x), 
                    (int) (r.srcOffset.y + r.srcSize.y), 
                    null
                ));
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
                int sWidth = (int) Math.ceil(sSize.x);
                int sHeight = (int) Math.ceil(sSize.y);
                Vec2 anchorOffset = r.anchor.clone()
                    .sub(r.srcOffset)
                    .div(r.srcSize)
                    .mul(sSize);
                Vec2 anchorSPos = conv.posOnScreen(worldPos.value.clone());
                Vec2 sPos = anchorSPos.clone().sub(anchorOffset);
                buffer.world.add(anchorSPos.y, g -> g.drawImage(
                    r.sprite.get(),
                    (int) sPos.x, (int) sPos.y, 
                    (int) sPos.x + sWidth, (int) sPos.y + sHeight,
                    (int) r.srcOffset.x, (int) r.srcOffset.y,
                    (int) (r.srcOffset.x + r.srcSize.x), 
                    (int) (r.srcOffset.y + r.srcSize.y), 
                    null
                ));
            }
        }
    }

}
