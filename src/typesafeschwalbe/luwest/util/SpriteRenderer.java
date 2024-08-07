
package typesafeschwalbe.luwest.util;

import java.awt.image.BufferedImage;

import typesafeschwalbe.luwest.engine.*;
import typesafeschwalbe.luwest.math.Vec2;

public class SpriteRenderer {

    public record Frame(Vec2 offset, Vec2 size, Vec2 anchor) {}

    public Resource<BufferedImage> texture;
    public final Frame[] frames;
    public long frameDelay;
    private long lastFrameTime;
    private int currentFrameIdx;
    public long pixelsPerUnit;
    public boolean renderReflection;

    public SpriteRenderer(
        Resource<BufferedImage> texture, 
        Frame[] frames, long frameDelay,
        long pixelsPerUnit,
        boolean renderReflection
    ) {
        this.texture = texture;
        this.frames = frames;
        this.frameDelay = frameDelay;
        this.lastFrameTime = System.currentTimeMillis();
        this.pixelsPerUnit = pixelsPerUnit;
        this.renderReflection = renderReflection;
    }

    public static void renderReflections(Scene scene) {
        for(Entity thing: scene.allWith(SpriteRenderer.class, Position.class)) {
            SpriteRenderer r = thing.get(SpriteRenderer.class);
            if(!r.renderReflection) { continue; }
            Frame frame = r.frames[r.currentFrameIdx];
            Position worldPos = thing.get(Position.class);
            for(Entity camera: scene.allWith(
                Camera.Conversion.class, Camera.Buffer.class
            )) {
                Camera.Conversion conv = camera.get(Camera.Conversion.class);
                Camera.Buffer buffer = camera.get(Camera.Buffer.class);
                int sWidth = (int) Math.ceil(conv.sizeOnScreen(
                    frame.size.x / r.pixelsPerUnit
                ));
                int sHeight = (int) Math.ceil(conv.sizeOnScreen(
                    frame.size.y / r.pixelsPerUnit
                ));
                Vec2 anchorOffset = frame.anchor.clone()
                    .sub(frame.offset)
                    .div(frame.size)
                    .mul(sWidth, sHeight);
                Vec2 anchorSPos = conv.posOnScreen(worldPos.value.clone());
                double sPosX = anchorSPos.x - anchorOffset.x;
                double sPosY = anchorSPos.y + Math.max(anchorOffset.y, sHeight);
                buffer.reflect.add(anchorSPos.y, g -> g.drawImage(
                    r.texture.get(),
                    (int) sPosX, (int) sPosY, 
                    (int) sPosX + sWidth, (int) sPosY - sHeight,
                    (int) frame.offset.x, (int) frame.offset.y,
                    (int) (frame.offset.x + frame.size.x), 
                    (int) (frame.offset.y + frame.size.y), 
                    null
                ));
            }
        }
    }

    public static void renderAll(Scene scene) {
        for(Entity thing: scene.allWith(SpriteRenderer.class, Position.class)) {
            SpriteRenderer r = thing.get(SpriteRenderer.class);
            Frame frame = r.frames[r.currentFrameIdx];
            Position worldPos = thing.get(Position.class);
            for(Entity camera: scene.allWith(
                Camera.Conversion.class, Camera.Buffer.class
            )) {
                Camera.Conversion conv = camera.get(Camera.Conversion.class);
                Camera.Buffer buffer = camera.get(Camera.Buffer.class);
                int sWidth = (int) Math.ceil(conv.sizeOnScreen(
                    frame.size.x / r.pixelsPerUnit
                ));
                int sHeight = (int) Math.ceil(conv.sizeOnScreen(
                    frame.size.y / r.pixelsPerUnit
                ));
                Vec2 anchorOffset = frame.anchor.clone()
                    .sub(frame.offset)
                    .div(frame.size)
                    .mul(sWidth, sHeight);
                Vec2 anchorSPos = conv.posOnScreen(worldPos.value.clone());
                Vec2 sPos = anchorSPos.clone().sub(anchorOffset);
                buffer.world.add(anchorSPos.y, g -> g.drawImage(
                    r.texture.get(),
                    (int) sPos.x, (int) sPos.y, 
                    (int) sPos.x + sWidth, (int) sPos.y + sHeight,
                    (int) frame.offset.x, (int) frame.offset.y,
                    (int) (frame.offset.x + frame.size.x), 
                    (int) (frame.offset.y + frame.size.y),
                    null
                ));
            }
        }
    }

    public static void updateFrames(Scene scene) {
        for(Entity thing: scene.allWith(SpriteRenderer.class)) {
            SpriteRenderer r = thing.get(SpriteRenderer.class);
            long diff = System.currentTimeMillis() - r.lastFrameTime;
            if(r.frameDelay != 0) {
                r.lastFrameTime += diff - (diff % r.frameDelay);
                r.currentFrameIdx += diff / r.frameDelay;
                r.currentFrameIdx %= r.frames.length;
            }
        }
    }

}
