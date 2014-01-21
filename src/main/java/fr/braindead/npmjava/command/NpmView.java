package fr.braindead.npmjava.command;

import com.google.gson.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: leiko
 * Date: 21/01/14
 * Time: 14:09
 */
public class NpmView {
    
    public void execute(String pattern, ViewCallback cb) {
        try {
            JsonObject data = getRemoteData(pattern);
            JsonObject viewResult = new JsonObject();
            
            if (data.has("error")) {
                cb.onError(new Exception("Unable to find "+pattern+" in npm registry"));

            } else {
                computeViewResult(data, viewResult);
                cb.onSuccess(viewResult);
            }
            
        } catch (Exception e) {
            cb.onError(e);
        }
    }
    
    public JsonObject execute(String pattern) throws Exception {       
        JsonObject data = getRemoteData(pattern);
        JsonObject viewResult = new JsonObject();

        if (data.has("error")) {
            throw new Exception("Unable to find "+pattern+" in npm registry");

        } else {
            computeViewResult(data, viewResult);
            return viewResult;
        }
    }
    
    private JsonObject getRemoteData(String pattern) throws IOException {
        URL url = new URL("http://registry.npmjs.org/"+pattern);
        URLConnection conn = url.openConnection();
        InputStream is = conn.getInputStream();
        JsonParser jsonParser = new JsonParser();
        JsonObject data = (JsonObject) jsonParser.parse(new InputStreamReader(is));
        return data;
    }
    
    private void computeViewResult(JsonObject data, JsonObject viewResult) {
        JsonObject versions = data.getAsJsonObject("versions");
        JsonPrimitive latest = data.getAsJsonObject("dist-tags").getAsJsonPrimitive("latest");
        JsonArray arrayVersions = new JsonArray();
        for (Map.Entry<String, JsonElement> entry : versions.entrySet()) {
            arrayVersions.add(new JsonPrimitive(entry.getKey()));
        }
        viewResult.add("versions", arrayVersions);
        viewResult.add("version", latest);
        // TODO add all other information based on "npm view" specification
    }
    
    public interface ViewCallback {
        void onSuccess(JsonObject result);
        void onError(Exception e);
    }
}
