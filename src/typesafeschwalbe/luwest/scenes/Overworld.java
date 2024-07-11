
package typesafeschwalbe.luwest.scenes;

import java.awt.Graphics2D;

import typesafeschwalbe.luwest.engine.*;
import typesafeschwalbe.luwest.math.Vec2;
import typesafeschwalbe.luwest.util.Camera;
import typesafeschwalbe.luwest.util.Position;

public class Overworld {

    public static class ExampleBoxRenderer {}

    public static void renderExampleBoxes(Scene scene) {
        for(Entity box: scene.allWith(
            ExampleBoxRenderer.class, Position.class
        )) {
            Position pos = box.get(Position.class);
            for(Entity camera: scene.allWith(
                Camera.Conversion.class, Camera.Buffer.class
            )) {
                Camera.Conversion conv = camera.get(Camera.Conversion.class);
                Camera.Buffer buffer = camera.get(Camera.Buffer.class);
                Graphics2D g = buffer.world.createGraphics();
                Vec2 boxPos = conv.posOnScreen(pos.value.clone());
                Vec2 boxSize = conv.sizeOnScreen(new Vec2(1.0, 1.0));
                g.setColor(java.awt.Color.BLUE);
                g.fillRect(
                    (int) (boxPos.x - boxSize.x / 2.0),
                    (int) (boxPos.y - boxSize.y), 
                    (int) (boxSize.x), 
                    (int) (boxSize.y)
                );
                g.dispose();
            }
        }
    }

    public static class ExampleMovement {}

    public static void doExampleMovement(Scene scene) {
        for(Entity thing: scene.allWith(
            ExampleMovement.class, Position.class
        )) {
            Position pos = thing.get(Position.class);
            for(Entity camera: scene.allWith(Camera.Conversion.class)) {
                Camera.Conversion conv = camera.get(Camera.Conversion.class);
                pos.value = conv
                    .posInWorld(Engine.window().mousePosition())
                    .add(0.0, 0.5);
            }
        }
        scene.setEnabled("render_boxes", Engine.window().mousePressed(1));
    }

    public static Scene createScene() {
        return new Scene("overworld.json")
            .with(new Entity()
                .with(ExampleBoxRenderer.class, new ExampleBoxRenderer())
                .with(Position.class, new Position())
                .with(ExampleMovement.class, new ExampleMovement())
            )
            .with(Camera.create(50.0))
            .with(Camera::resizeBuffers, Camera::computeOffsets)
            .with(Overworld::doExampleMovement)
            .with("render_boxes", Overworld::renderExampleBoxes)
            .with(Camera::showBuffers);
    }

}