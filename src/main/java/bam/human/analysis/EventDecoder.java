package bam.human.analysis;

import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public interface EventDecoder {

    static EventDecoder JSON() {
        return (InputStream input) -> {
            try {

                // Build json array
                JSONArray json = new JSONArray(IOUtils.toString(input, "UTF-8"));

                // Move events to list
                List<JSONObject> events = new ArrayList<>();

                for(int index = 0; index < json.length(); ++index)
                    events.add(json.getJSONObject(index));

                return events;
            } catch(IOException|JSONException e) {
                System.out.println("failed to parse events from: " + input.toString());

                return new LinkedList<>();
            }
        };
    }

    static EventDecoder compressedJSON() {
        CompressorStreamFactory factory = new CompressorStreamFactory();
        EventDecoder json = JSON();

        return (InputStream input) -> {
            try {

                // Initialize decompression stream
                CompressorInputStream expand = factory.createCompressorInputStream(CompressorStreamFactory.GZIP, input);

                // Parse events
                return json.events(expand);
            } catch(CompressorException e) {
                System.out.println("failed to parse events from: " + input.toString());

                return new LinkedList<>();
            }
        };
    }

    List<JSONObject> events(InputStream input);
}
