package bam;

import jdk.jshell.JShell;
import jdk.jshell.JShellException;
import jdk.jshell.SnippetEvent;
import jdk.jshell.SourceCodeAnalysis;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

/**
 * This class serves as an entry point for JShell
 * scripts that use the bam package.
 */
public class ScriptMain {

    public static void main(String[] args) {
        /* if(0 == args.length) {
            System.out.println("No script file specified");
            System.exit(0);
        }*/

        // Not sure about the best way to test this
        run("C:\\Users\\Tyler\\Desktop\\test_script.txt");
    }

    private static void run(String path) {

        // Start shell
        try (JShell shell = JShell.create()){

            // Extend classpath to include the current program's class path
            shell.addToClasspath(System.getProperty("java.class.path"));

            // Load script
            String script = FileUtils.readFileToString(Paths.get(path).toFile(), "UTF-8");

            // Parse and execute script
            SourceCodeAnalysis.CompletionInfo statement;

            do {
                // Get the next statement
                statement = shell.sourceCodeAnalysis().analyzeCompletion(script);

                // Check if the next statement is valid
                if(!statement.completeness().isComplete())
                    throw new Exception("Incomplete statement: " + statement.source());

                // Evaluate the statement
                List<SnippetEvent> events = shell.eval(statement.source());

                // Check results
                for(SnippetEvent event : events) {
                    if(null != event.exception()) {
                        throw event.exception();
                    } else if(null != event.value()) {
                        System.out.println(event.value());
                    }
                }

                // Get remaining code
                script = statement.remaining();

            } while(!statement.remaining().isEmpty());
        } catch(IOException e) {
            System.out.println("IO ERROR: " + e.getMessage());
        } catch(JShellException e) {
            System.out.println("SHELL ERROR: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }
}
