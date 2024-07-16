
package typesafeschwalbe.luwest.util;

import java.awt.event.MouseEvent;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Optional;

import typesafeschwalbe.luwest.engine.*;
import typesafeschwalbe.luwest.math.Vec2;

public class Editor {

    public final String editedPath;
    private final StaticScene staticScene;
    private final Sectors.Observer observer;
    public final Scene scene;

    public Editor(String editedPath) {
        this.editedPath = editedPath;
        this.staticScene = new StaticScene(editedPath, Resource.EXTERNAL);
        this.observer = new Sectors.Observer(this.staticScene, 4);
        this.scene = new Scene()
            .with(
                Camera.create(Editor.CAMERA_ZOOM_SPEED * 4.0)
                    .with(Sectors.Observer.class, observer)
                    .with(Velocity.class, new Velocity())
            )
            .with(
                Sectors::manageAll,
                Editor::moveCamera,
                Editor::zoomCamera,

                Camera::computeOffsets,

                SpriteRenderer::renderReflections,
                Camera::renderReflections,

                staticScene::renderBackground,
                SpriteRenderer::renderAll,
                Camera::renderAll,
                
                Camera::showBuffers
            );
    }

    public String serializeScene() {
        HashSet<Sectors.Sector> reset = new HashSet<>();
        for(Entity entity: this.scene.allWith(
            Position.class, Sectors.Owned.class
        )) {
            Position position = entity.get(Position.class);
            Sectors.Sector sector = this.observer.asSector(position.value);
            if(reset.contains(sector)) { continue; }
            this.staticScene.serializeSector(sector.x, sector.y, this.observer, this.scene);
            reset.add(sector);
        }
        return this.staticScene.serialize();
    }

    // /!\ IMPORTANT /!\
    // THIS SHOULD RUN EVERY TIME SOMETHING IS MODIFED :)
    public void saveScene() {
        try(PrintWriter pw = new PrintWriter(this.editedPath)) {
            pw.println(this.serializeScene());
        } catch(FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


    private static record CameraAnchor(Vec2 mouse, Vec2 world) {}

    public static Optional<CameraAnchor> CAMERA_ANCHOR = Optional.empty();

    public static void moveCamera(Scene scene) {
        for(Entity camera: scene.allWith(
            Camera.Conversion.class, Position.class
        )) {
            Camera.Conversion conversion = camera.get(Camera.Conversion.class);
            Position position = camera.get(Position.class);
            if(Engine.window().mousePressed(MouseEvent.BUTTON2)) {
                if(Editor.CAMERA_ANCHOR.isEmpty()) {
                    Editor.CAMERA_ANCHOR = Optional.of(new CameraAnchor(
                        Engine.window().mousePosition(), 
                        position.value.clone()
                    ));
                }
                Vec2 mouseDiff = Engine.window().mousePosition()
                    .sub(Editor.CAMERA_ANCHOR.get().mouse());
                position.value = Editor.CAMERA_ANCHOR.get().world().clone()
                    .sub(conversion.sizeInWorld(mouseDiff));
            } else {
                Editor.CAMERA_ANCHOR = Optional.empty();
            }
        }
    }

    public static final double CAMERA_ZOOM_SPEED = 5.0;
    public static final double CAMERA_MIN_ZOOM = 1.0;
    public static final double CAMERA_MAX_ZOOM = 50.0;

    public static void zoomCamera(Scene scene) {
        for(Entity camera: scene.allWith(Camera.Configuration.class)) {
            Camera.Configuration config = camera.get(Camera.Configuration.class);
            config.distance = Math.clamp(
                config.distance 
                    + Engine.window().scrollOffset() * Editor.CAMERA_ZOOM_SPEED,
                Editor.CAMERA_MIN_ZOOM * Editor.CAMERA_ZOOM_SPEED,
                Editor.CAMERA_MAX_ZOOM * Editor.CAMERA_ZOOM_SPEED
            );
        }
        Engine.window().resetScrollOffset();
    }

}
