package fr.braindead.npmjava.command;

import com.google.gson.*;
import fr.braindead.npmjava.UpdateNpmCache;
import fr.braindead.npmjava.util.Conf;

import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: leiko
 * Date: 20/01/14
 * Time: 18:01
 */
public class NpmSearch {

    private static final long CACHE_TIMEOUT = 60000;

    public JsonArray execute(String pattern) throws Exception {
        // First try to reach cached file to gain some precious time
        File cache = new File(Conf.getNpmCachePath());
        if (cache.exists()) {
            JsonParser jsonParser = new JsonParser();
            JsonObject cacheData = (JsonObject) jsonParser.parse(new FileReader(cache));

            // there is an npm cache file
            if (System.currentTimeMillis() - cache.lastModified() < CACHE_TIMEOUT) {
                // no need to check remote registry, local cache seems up to date
                return getSearchResults(cacheData, pattern);

            } else {
                // local cache seems out of date, lets check remote registry
                URL url = new URL("http://registry.npmjs.org/-/all/since?stale=update_after&startkey="+cache.lastModified());
                URLConnection conn = url.openConnection();
                InputStream is = conn.getInputStream();
                JsonObject data = (JsonObject) jsonParser.parse(new InputStreamReader(is));
                for (Map.Entry<String, JsonElement> entry : data.entrySet()) {
                    cacheData.add(entry.getKey(), entry.getValue());
                }

                UpdateNpmCache npmCache = new UpdateNpmCache(cacheData);
                return getSearchResults(npmCache.getNpmDb(), pattern);
            }

        } else {
            // unable to find cache...retrieve the whole npm registry db (this is going to take a loooong time)
            System.out.println("Updating npm cache for the first time. Please, be patient.");
            UpdateNpmCache npmCache = new UpdateNpmCache();
            return getSearchResults(npmCache.getNpmDb(), pattern);
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

    public static void main(String[] args) throws Exception {
        NpmSearch search = new NpmSearch();
        JsonArray array = search.execute("(^kevoree-chan-|^kevoree-node-|^kevoree-group-|^kevoree-comp-).*");
        System.out.println(array.get(0).getAsJsonObject().toString());
    }
}
