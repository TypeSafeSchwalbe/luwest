
package typesafeschwalbe.luwest.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.FileNotFoundException;
import java.io.PrintWriter; 
import java.util.HashSet;
import java.util.Optional;
import java.util.function.Consumer;

import typesafeschwalbe.luwest.engine.*;
import typesafeschwalbe.luwest.math.Vec2;

public class Editor {

    public final String editedPath;
    private final StaticScene staticScene;
    private final Sectors.Observer observer;
    public final Scene scene;

    private final Resource<Font> font;

    public Editor(String editedPath, Scene.System... renderers) {
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
                this::manageTextInput,
                Sectors::manageAll,

                Editor::moveCamera,
                Editor::zoomCamera,

                this::selectMode,
                this::updateMode,
 
                Camera::computeOffsets
            )
            .with(
                SpriteRenderer::renderReflections,
                Camera::renderReflections,
    
                staticScene::renderBackground,
                SpriteRenderer::renderAll
            )
            .with(renderers)
            .with(
                this::renderTextInput,
                this::renderMode,
                this::renderMousePosition,
                this::renderUnsavedChanges,
                Camera::renderAll,

                Camera::showBuffers
            );
        this.font = Resource.ttfFont(
            "res/fonts/jetbrains_mono.ttf", Resource.EMBEDDED
        );
    }


    private boolean hasUnsavedChanges = false;

    // /!\ IMPORTANT /!\
    // THIS SHOULD RUN EVERY TIME THE SCENE IS MODIFIED!
    public void serializeSceneUpdates() {
        for(Sectors.Sector sector: this.observer.observedSectors()) {
            this.staticScene.serializeSector(
                sector.x, sector.y, this.observer, this.scene
            );
        }
        this.hasUnsavedChanges = true;
    }

    public void saveScene() {
        try(PrintWriter pw = new PrintWriter(this.editedPath)) {
            pw.println(this.staticScene.serialize());
        } catch(FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        this.hasUnsavedChanges = false;
    }

    public void renderUnsavedChanges(Scene scene) {
        if(!this.hasUnsavedChanges) { return; }
        for(Entity camera: scene.allWith(Camera.Buffer.class)) {
            Camera.Buffer buffer = camera.get(Camera.Buffer.class);
            buffer.world.add(Double.POSITIVE_INFINITY, g -> {
                g.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING, 
                    RenderingHints.VALUE_ANTIALIAS_ON
                );
                g.setFont(this.font.get().deriveFont(Font.BOLD, 15));
                g.setColor(Color.BLACK);
                g.drawString(
                    "unsaved changes", 
                    10, 10 + 20 + 10 + 15
                ); 
            });
        }
    }


    private boolean inTextInput = false;
    private Consumer<String> onInputComplete = i -> {};
    
    private void beginTextInput(Consumer<String> onInputComplete) {
        Engine.window().resetTypedText();
        this.inTextInput = true;
        this.onInputComplete = onInputComplete;
    }

    private void manageTextInput(Scene scene) {
        if(!this.inTextInput) { return; }
        if(Engine.window().keyPressed(KeyEvent.VK_ENTER)) {
            this.inTextInput = false;
            this.onInputComplete.accept(Engine.window().typedText());
        }
    }

    private void renderTextInput(Scene scene) {
        if(!this.inTextInput) { return; }
        for(Entity camera: scene.allWith(Camera.Buffer.class)) {
            Camera.Buffer buffer = camera.get(Camera.Buffer.class);
            buffer.world.add(Double.POSITIVE_INFINITY, g -> {
                g.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING, 
                    RenderingHints.VALUE_ANTIALIAS_ON
                );
                g.setFont(this.font.get().deriveFont(Font.BOLD, 20));
                g.setColor(Color.BLACK);
                g.drawString(
                    "> " + Engine.window().typedText(), 
                    10, Engine.window().height() - 10 - 20 - 10
                ); 
            });
        }
    }


    public interface EditorMode {
        String getName();
        default void update(Scene scene, Editor editor) {}
        default void render(Scene scene, Editor editor) {}
        default void exit(Scene scene, Editor editor) {}
    }

    private EditorMode currentMode = new SelectEntityMode();

    private void setMode(EditorMode mode) {
        this.currentMode.exit(this.scene, this);
        this.currentMode = mode;
    }

    private void selectMode(Scene scene) {
        if(this.inTextInput) { return; }
        if(Engine.window().keyPressed(KeyEvent.VK_CONTROL)) {
            if(Engine.window().keyPressed(KeyEvent.VK_S)) {
                this.saveScene();
            }
        } else {
            if(Engine.window().keyPressed(KeyEvent.VK_C)) {
                this.beginTextInput(entityType -> {
                    this.setMode(new CreateEntityMode(
                        entityType.trim(), this
                    ));
                });
            } else if(Engine.window().keyPressed(KeyEvent.VK_S)) {
                this.setMode(new SelectEntityMode());
            }
        }
    }

    private void updateMode(Scene scene) {
        if(this.inTextInput) { return; }
        this.currentMode.update(scene, this);
    }

    private void renderMode(Scene scene) {
        if(!this.inTextInput) {
            this.currentMode.render(scene, this);
        }
        for(Entity camera: scene.allWith(Camera.Buffer.class)) {
            Camera.Buffer buffer = camera.get(Camera.Buffer.class);
            buffer.world.add(Double.POSITIVE_INFINITY, g -> {
                g.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING, 
                    RenderingHints.VALUE_ANTIALIAS_ON
                );
                g.setFont(this.font.get().deriveFont(Font.BOLD, 20));
                g.setColor(Color.BLACK);
                g.drawString(
                    this.currentMode.getName(), 
                    10, 10 + 20
                );
            });
        }
    }


    private void renderMousePosition(Scene scene) {
        for(Entity camera: scene.allWith(
            Camera.Buffer.class, Camera.Conversion.class
        )) {
            Camera.Buffer buffer = camera.get(Camera.Buffer.class);
            Camera.Conversion conv = camera.get(Camera.Conversion.class);
            buffer.world.add(Double.POSITIVE_INFINITY, g -> {
                g.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING, 
                    RenderingHints.VALUE_ANTIALIAS_ON
                );
                g.setFont(this.font.get().deriveFont(Font.BOLD, 20));
                g.setColor(Color.BLACK);
                Vec2 worldPos = conv
                    .posInWorld(Engine.window().mousePosition());
                g.drawString(
                    ((long) (worldPos.x * 100.0) / 100.0) 
                        + " " 
                        + ((long) (worldPos.y * 100.0) / 100.0),
                    10, Engine.window().height() - 10
                );
            });
        }
    }


    private static record CameraAnchor(Vec2 mouse, Vec2 world) {}

    private static Optional<CameraAnchor> CAMERA_ANCHOR = Optional.empty();

    private static void moveCamera(Scene scene) {
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

    private static final double CAMERA_ZOOM_SPEED = 5.0;
    private static final double CAMERA_MIN_ZOOM = 1.0;
    private static final double CAMERA_MAX_ZOOM = 50.0;

    private static void zoomCamera(Scene scene) {
        for(Entity camera: scene.allWith(Camera.Configuration.class)) {
            Camera.Configuration conf = camera.get(Camera.Configuration.class);
            conf.distance = Math.clamp(
                conf.distance 
                    + Engine.window().scrollOffset() * Editor.CAMERA_ZOOM_SPEED,
                Editor.CAMERA_MIN_ZOOM * Editor.CAMERA_ZOOM_SPEED,
                Editor.CAMERA_MAX_ZOOM * Editor.CAMERA_ZOOM_SPEED
            );
        }
        Engine.window().resetScrollOffset();
    }


    private static class CreateEntityMode implements EditorMode {
        private final String entityType;
        private Entity preview;
        private boolean placementAllowed = true;
        private final double stepSize;

        private void createPreview(Scene scene) {
            this.preview = Serialization
                .createInstance(entityType, Resource.EXTERNAL);
            scene.with(this.preview);
        }

        CreateEntityMode(String entityType, Editor editor) {
            this.entityType = entityType;
            this.createPreview(editor.scene);
            this.stepSize = 1.0 
                / editor.staticScene.json.get("grid_density").getAsLong();
        }

        @Override
        public String getName() { return "create " + this.entityType; }

        @Override
        public void update(Scene scene, Editor editor) {
            if(this.preview.has(Position.class)) {
                Position position = this.preview.get(Position.class);
                for(Entity camera: scene.allWith(Camera.Conversion.class)) {
                    Camera.Conversion conv = camera
                        .get(Camera.Conversion.class);
                    position.value = conv
                        .posInWorld(Engine.window().mousePosition())
                        .div(this.stepSize)
                        .map(e -> (long) e)
                        .mul(this.stepSize);
                }
            }
            boolean shouldPlaceEntity = this.placementAllowed
                && Engine.window().mousePressed(MouseEvent.BUTTON1);
            if(shouldPlaceEntity) {
                this.preview.with(Sectors.Owned.class, new Sectors.Owned());
                this.createPreview(scene);
                editor.serializeSceneUpdates();
                this.placementAllowed = false;
            }
            if(!Engine.window().mousePressed(MouseEvent.BUTTON1)) {
                this.placementAllowed = true;
            }
        }

        @Override
        public void exit(Scene scene, Editor editor) {
            scene.remove(this.preview);
        }
    }


    private static class SelectEntityMode implements EditorMode {
        private enum SelectionType {
            REPLACE,
            ADD,
            SUBTRACT;
        }

        @Override
        public String getName() { return "select"; }

        private HashSet<Entity> selected = new HashSet<>();
        private Optional<Vec2> selectionStart = Optional.empty();
        private Optional<SelectionType> selectionType = Optional.empty();

        private void handleSelections(Scene scene, Editor editor) {
            boolean startSelection = selectionStart.isEmpty() 
                && Engine.window().mousePressed(MouseEvent.BUTTON1);
            if(startSelection) {
                this.selectionStart = Optional
                    .of(Engine.window().mousePosition());
                if(Engine.window().keyPressed(KeyEvent.VK_SHIFT)) {
                    this.selectionType = Optional.of(SelectionType.ADD);
                } else if(Engine.window().keyPressed(KeyEvent.VK_ALT)) {
                    this.selectionType = Optional.of(SelectionType.SUBTRACT);
                } else {
                    this.selectionType = Optional.of(SelectionType.REPLACE);    
                }
            }
            boolean endSelection = selectionStart.isPresent()
                && !Engine.window().mousePressed(MouseEvent.BUTTON1);
            if(endSelection) {
                for(Entity camera: scene.allWith(Camera.Conversion.class)) {
                    Camera.Conversion conv = camera
                        .get(Camera.Conversion.class);
                    Vec2 boundA = conv.posInWorld(this.selectionStart.get());
                    Vec2 boundB = conv
                        .posInWorld(Engine.window().mousePosition());
                    if(this.selectionType.get() == SelectionType.REPLACE) {
                        this.selected.clear();
                    }
                    for(Entity selected: scene.allWith(Sectors.Owned.class)) {
                        Vec2 pos = selected.get(Position.class).value;
                        if(pos.x < Math.min(boundA.x, boundB.x)) { continue; }
                        if(pos.x > Math.max(boundA.x, boundB.x)) { continue; }
                        if(pos.y < Math.min(boundA.y, boundB.y)) { continue; }
                        if(pos.y > Math.max(boundA.y, boundB.y)) { continue; }
                        switch(this.selectionType.get()) {
                            case REPLACE:
                            case ADD: {
                                this.selected.add(selected);
                            } break;
                            case SUBTRACT: {
                                this.selected.remove(selected);
                            } break;
                        }
                    }
                }
                selectionStart = Optional.empty();
            }
        }

        private void deleteSelected(Scene scene, Editor editor) {
            boolean doDeletion = Engine.window().keyPressed(KeyEvent.VK_DELETE)
                || Engine.window().keyPressed(KeyEvent.VK_BACK_SPACE);
            if(!doDeletion) { return; }
            for(Entity deleted: this.selected) {
                scene.remove(deleted);
            }
            if(this.selected.size() > 0) {
                scene.afterFrame(editor::serializeSceneUpdates);
            }
            this.selected.clear();
        }

        private boolean movementAllowed = true;
        private Optional<Vec2> moveLastPos = Optional.empty();

        private void moveSelected(Scene scene, Editor editor) {
            boolean startMovement = this.movementAllowed
                && this.moveLastPos.isEmpty()
                && Engine.window().mousePressed(MouseEvent.BUTTON3);
            boolean continueMovement = this.moveLastPos.isPresent()
                && Engine.window().mousePressed(MouseEvent.BUTTON3);
            if(startMovement) {
                this.moveLastPos = Optional.of(Engine.window().mousePosition());
                this.movementAllowed = false;
            } else if(continueMovement) {
                Vec2 screenOffset = Engine.window().mousePosition()
                    .sub(this.moveLastPos.get());
                this.moveLastPos = Optional.of(Engine.window().mousePosition());
                for(Entity camera: scene.allWith(Camera.Conversion.class)) {
                    Camera.Conversion conv = camera
                        .get(Camera.Conversion.class);
                    Vec2 offset = conv.sizeInWorld(screenOffset);
                    for(Entity entity: this.selected) {
                        entity.get(Position.class).value.add(offset);
                    }
                }
                if(this.selected.size() > 0) {
                    editor.serializeSceneUpdates();
                }
            } else if(!Engine.window().mousePressed(MouseEvent.BUTTON3)) {
                this.moveLastPos = Optional.empty();
                this.movementAllowed = true;
            }
        }

        @Override
        public void update(Scene scene, Editor editor) {
            this.handleSelections(scene, editor);
            this.deleteSelected(scene, editor);
            this.moveSelected(scene, editor);
        }

        @Override
        public void render(Scene scene, Editor editor) {
            for(Entity camera: scene.allWith(
                Camera.Buffer.class, Camera.Conversion.class
            )) {
                Camera.Conversion conv = camera.get(Camera.Conversion.class);
                Camera.Buffer buff = camera.get(Camera.Buffer.class);
                for(Entity entity: scene.allWith(Sectors.Owned.class)) {
                    Vec2 pos = conv
                        .posOnScreen(entity.get(Position.class).value.clone());
                    buff.world.add(Double.POSITIVE_INFINITY, g -> {
                        g.setColor(Color.WHITE);
                        if(this.selected.contains(entity)) {
                            g.fillRect(
                                (int) pos.x - 10, (int) pos.y - 10, 20, 20
                            );
                        } else {
                            g.drawRect(
                                (int) pos.x - 10, (int) pos.y - 10, 20, 20
                            );
                        }
                    });
                }
                if(this.selectionStart.isPresent()) {
                    buff.world.add(Double.POSITIVE_INFINITY, g -> {
                        g.setColor(Color.WHITE);
                        Vec2 boundA = this.selectionStart.get();
                        Vec2 boundB = Engine.window().mousePosition();
                        int posX = (int) Math.min(boundA.x, boundB.x);
                        int posY = (int) Math.min(boundA.y, boundB.y);
                        int sizeX = (int) (Math.max(boundA.x, boundB.x) - posX);
                        int sizeY = (int) (Math.max(boundA.y, boundB.y) - posY);
                        g.drawRect(posX, posY, sizeX, sizeY);
                    });
                }
            }
        }
    }

}
