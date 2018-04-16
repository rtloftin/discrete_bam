package bam;

import jdk.jshell.JShell;
import jdk.jshell.JShellException;
import jdk.jshell.SnippetEvent;
import jdk.jshell.SourceCodeAnalysis;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.Paths;
import java.util.List;

/**
 * This class serves as an entry point for JShell
 * scripts that use the bam package.
 */
public class ScriptMain {

    public static void main(String[] args) throws Exception {

        // Build the json object
        JSONArray array = new JSONArray(new double[200000]);

        JSONObject json = new JSONObject();
        json.put("text", "compression test");
        json.put("array", array);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        CompressorOutputStream compress = new CompressorStreamFactory().createCompressorOutputStream(CompressorStreamFactory.GZIP, output);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(compress, "UTF-8"));
        json.write(writer);
        writer.flush();
        writer.close();

        byte[] data = output.toByteArray();
        System.out.println(data.length + " bytes");

        ByteArrayInputStream input = new ByteArrayInputStream(data);
        CompressorInputStream expand = new CompressorStreamFactory().createCompressorInputStream(CompressorStreamFactory.GZIP, input);

        JSONObject new_json = new JSONObject(IOUtils.toString(new InputStreamReader(expand, "UTF-8")));

        System.out.println(new_json.getString("text"));

        // Not sure about the best way to test this
        // run("C:\\Users\\Tyler\\Desktop\\test_script.txt");
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
