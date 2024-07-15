
package typesafeschwalbe.luwest;

import java.io.File;
import java.io.PrintWriter;

import javax.swing.JOptionPane;

import typesafeschwalbe.luwest.scenes.Overworld;
import typesafeschwalbe.luwest.util.Editor;
import typesafeschwalbe.luwest.engine.Engine;
import typesafeschwalbe.luwest.engine.Scene;

public class Main {
    
    public static void main(String[] args) {
        try {
            String title = null;
            Scene scene = null;
            for(int argI = 0; argI < args.length - 1; argI += 1) {
                if(args[argI].equals("--edit")) {
                    title = "Luwest Editor";
                    scene = Editor.createScene(args[argI + 1]);
                }
            }
            if(title == null) {
                title = "Luwest";
                scene = Overworld.createScene();
            }
            Engine.init(title);
            Engine.setScene(scene);
            Engine.start();
        } catch(Exception e) {
            Main.handleCrash(e);
            System.exit(1);
        }
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