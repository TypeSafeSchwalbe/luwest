
package typesafeschwalbe.luwest;

import java.io.File;
import java.io.PrintWriter;

import javax.swing.JOptionPane;

import typesafeschwalbe.luwest.scenes.*;
import typesafeschwalbe.luwest.util.Editor;
import typesafeschwalbe.luwest.util.Serialization;
import typesafeschwalbe.luwest.engine.Engine;

public class Main {
    
    public static void main(String[] args) {
        try {
            for(int argI = 0; argI < args.length - 1; argI += 1) {
                if(args[argI].equals("--edit")) {
                    Main.startEditor(args[argI + 1]);
                    return;
                }
            }
            Main.startGame();
        } catch(Exception e) {
            Main.handleCrash(e);
            System.exit(1);
        }
    }

    private static void startEditor(String editedPath) {
        Serialization.define("lake", new Lake.LakeSerializer());
        Engine.init("Luwest Editor");
        Engine.setScene(new Editor(
            editedPath,
            Lake::renderAll
        ).scene);
        Engine.start();
    }

    private static void startGame() {
        Engine.init("Luwest");
        Engine.setScene(Overworld.createScene());
        Engine.start();
    }

    private static void handleCrash(Exception e) {
        String message = e.getClass().getName() 
            + ": " + e.getMessage() + "\n";
        File report = new File("crash_" + System.currentTimeMillis() + ".txt");
        try(PrintWriter pw = new PrintWriter(report)) {
            e.printStackTrace(pw);
            message += "Further information has been written to\n'"
                + report.getCanonicalPath() + "'.";
        } catch(Exception fe) {
            message += "Failed to dump further information.";
        }
        JOptionPane.showMessageDialog(
            null, message, "Game Crash", JOptionPane.PLAIN_MESSAGE
        );
    }

}