package fr.braindead.npmjava.command;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import fr.braindead.npmjava.UpdateNpmCache;
import fr.braindead.npmjava.util.Conf;

import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: leiko
 * Date: 20/01/14
 * Time: 18:01
 */
public class NpmSearch {

    private static final long CACHE_TIMEOUT = 60000;
    
    public void execute(final String pattern, final SearchCallback cb) {
        try {
            // First try to reach cached file to gain some precious time
            File cache = new File(Conf.getNpmCachePath());
            if (cache.exists()) {
                JsonParser jsonParser = new JsonParser();
                JsonObject cacheData = (JsonObject) jsonParser.parse(new FileReader(cache));
                
                // there is an npm cache file
                if (System.currentTimeMillis() - cache.lastModified() < CACHE_TIMEOUT) {
                    // no need to check remote registry, local cache seems up to date
                    cb.onSuccess(getSearchResults(cacheData, pattern));

                } else {                   
                    // local cache seems out of date, lets check remote registry
                    URL url = new URL("http://registry.npmjs.org/-/all/since?stale=update_after&startkey="+cache.lastModified());
                    URLConnection conn = url.openConnection();
                    InputStream is = conn.getInputStream();
                    JsonObject data = (JsonObject) jsonParser.parse(new InputStreamReader(is));
                    for (Map.Entry<String, JsonElement> entry : data.entrySet()) {
                        cacheData.add(entry.getKey(), entry.getValue());
                    }
                    cb.onSuccess(getSearchResults(cacheData, pattern));
                    
                    // update cache in background
                    UpdateNpmCache thread = new UpdateNpmCache(cacheData);
                    thread.start();
                }

            } else {
                // unable to find cache...retrieve the whole npm registry db (this is going to take a loooong time)
                System.out.println("Update npm cache for the first time. Please, be patient.");
                UpdateNpmCache thread = new UpdateNpmCache(new UpdateNpmCache.DoneCallback() {
                    @Override
                    public void done(JsonObject npmdb) {
                        cb.onSuccess(getSearchResults(npmdb, pattern));
                    }
                    
                    @Override
                    public void error(Exception e) {
                        cb.onError(e);
                    }
                });
                thread.start();
            }
            
        } catch (Exception e) {
            cb.onError(e);
        }
    }
    
    private JsonArray getSearchResults(JsonObject cacheData, String pattern) {
        JsonArray result = new JsonArray();
        // cacheData = {<name>: {<package data>}}
        for (Map.Entry<String, JsonElement> entry : cacheData.entrySet()) {
            if (entry.getKey().matches(pattern)) {
                result.add(entry.getValue());
            }
        }
        return result;
    }
    
    public interface SearchCallback {
        void onSuccess(JsonArray jsonRes);
        void onError(Exception e);
    }

    public static void main(String[] args) {
        NpmSearch search = new NpmSearch();
        search.execute("(^kevoree-chan-|^kevoree-node-|^kevoree-group-|^kevoree-comp-).*", new SearchCallback() {
            @Override
            public void onSuccess(JsonArray jsonRes) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                System.out.println(gson.toJson(jsonRes));
            }

            @Override
            public void onError(Exception e) {
                System.err.println("ERROR");
                e.printStackTrace();
            }
        });
    }
}
