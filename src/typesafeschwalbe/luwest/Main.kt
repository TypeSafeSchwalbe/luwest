
package typesafeschwalbe.luwest

import kotlin.system.exitProcess

import java.io.PrintWriter
import java.io.File
import javax.swing.JOptionPane

import typesafeschwalbe.luwest.engine.Engine
import typesafeschwalbe.luwest.util.Editor;
import typesafeschwalbe.luwest.util.Serialization;
import typesafeschwalbe.luwest.scenes.*

fun defineSerializers() {
    Serialization.define("lake", Lake.LakeSerializer())
}
fun main(args: Array<String>) {
    defineSerializers()
    val edited = (1..<args.size).asSequence()
        .find { i -> args[i - 1] == "--edit" }
    try {
        if(edited == null) {
            startGame()
        } else {
            startEditor(args[edited])
        }
    } catch(e: Exception) {
        handleException(e)
        exitProcess(1)
    }
}

fun startEditor(edited: String) {
    Engine.init("Luwest Editor")
    Engine.setScene(Editor(
        edited, 
        Lake::renderAll
    ).scene)
    Engine.start()
}

fun startGame() {
    Engine.init("Luwest")
    Engine.setScene(overworld())
    Engine.start()
}

fun handleException(e: Exception) {
    var msg = "${e.javaClass.getName()}: ${e.message}\n"
    val traceFile = File("crash_${System.currentTimeMillis()}.txt")
    try {
        PrintWriter(traceFile).use { pw -> e.printStackTrace(pw) }
        msg += "Further information has been written to\n"
        msg += "${traceFile.canonicalPath}."
    } catch(tde: Exception) {
        msg += "Failed to dump a stack trace."
    }
    JOptionPane.showMessageDialog(
        null, msg, "Luwest Crash Handler", JOptionPane.PLAIN_MESSAGE 
    )
}