
package typesafeschwalbe.luwest;

import java.io.File;
import java.io.PrintWriter;

import javax.swing.JOptionPane;

import typesafeschwalbe.luwest.engine.Engine;
import typesafeschwalbe.luwest.scenes.Overworld;

public class Main {
    
    public static void main(String[] args) {
        try {
            Engine.init("Luwest");
            Engine.setScene(Overworld.createScene());
            Engine.start();
        } catch(Exception e) {
            Main.handleCrash(e);
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