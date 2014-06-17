package fr.braindead.npmjava;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.braindead.npmjava.util.Conf;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created with IntelliJ IDEA.
 * User: leiko
 * Date: 20/01/14
 * Time: 17:41
 */
public class UpdateNpmCache extends Thread {

    private JsonObject jsonCache = null;

    public UpdateNpmCache() {
        this(null);
    }

    /**
     * Update npm cache from a given JsonObject
     * @param jsonCache content used to overwrite cache file
     */
    public UpdateNpmCache(JsonObject jsonCache) {
        this.jsonCache = jsonCache;
    }

    public JsonObject getNpmDb() throws Exception {
        File cache = new File(Conf.getNpmCachePath());
        if (!cache.exists()) {
            cache.getParentFile().mkdirs();
            cache.createNewFile();
            return writeCache(cache);

        }

        return writeCache(cache);
    }

    private JsonObject writeCache(File cache) throws Exception {
        InputStream is;
        FileOutputStream fos;
        long start = System.currentTimeMillis();

        // TODO if JVM is stop while writing file... file will be corrupted and I do not handle corrupted file yet        
        if (this.jsonCache != null) {
            // update cache file from given JsonObject
            is = new ByteArrayInputStream(this.jsonCache.toString().getBytes());

        } else {
            // update cache from remote registry (this may take a long time)
            URL url = new URL("http://registry.npmjs.org/-/all");
            URLConnection conn = url.openConnection();
            is = conn.getInputStream();
        }

        fos = new FileOutputStream(cache);

        byte[] buffer = new byte[4096];
        int len;
        while ((len = is.read(buffer)) != -1) {
            fos.write(buffer, 0, len);
        }

        System.out.println("Update npm cache done in " + (System.currentTimeMillis() - start) + "ms");
        is.close();
        fos.close();

        JsonParser parser = new JsonParser();
        return parser.parse(new FileReader(cache)).getAsJsonObject();
    }
}
