package fr.braindead.npmjava;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import fr.braindead.npmjava.util.Conf;
import org.kevoree.log.Log;

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

    private DoneCallback cb;
    
    public UpdateNpmCache() {
        this.cb = new DoneCallback() {
            @Override
            public void done(JsonObject npmdb) {}
            @Override
            public void error(Exception e) {}
        };
    }

    public UpdateNpmCache(DoneCallback cb) {
        this.cb = cb;
    }

    @Override
    public void run() {
        Log.debug("Update npm cache thread started");
        File cache = new File(Conf.getNpmCachePath());
        if (!cache.exists()) {
            cache.getParentFile().mkdirs();
            try {
                cache.createNewFile();
                writeCache(cache);

            } catch (IOException e) {
                Log.error(e.getMessage());
            }
        } else {
            writeCache(cache);
        }
    }

    private void writeCache(File cache) {
        InputStream is = null;
        FileOutputStream fos = null;
        long start = System.currentTimeMillis();

        try {
            URL url = new URL("http://registry.npmjs.org/-/all");

            URLConnection conn = url.openConnection();
            is = conn.getInputStream();
            fos = new FileOutputStream(cache);

            byte[] buffer = new byte[4096];
            int len;
            while ((len = is.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
            }

        } catch (Exception e) {
            Log.error(e.getMessage());

        } finally {
            Log.debug("Update npm cache thread done in " + (System.currentTimeMillis() - start) + "ms");
            try {
                if (is != null) is.close();
                if (fos != null) fos.close();
                JsonParser parser = new JsonParser();
                JsonObject npmdb = (JsonObject) parser.parse(new FileReader(cache));
                cb.done(npmdb);
                
            } catch (Exception e) {
                cb.error(e);
            }
        }
    }
    
    public interface DoneCallback {
        void done(JsonObject npmdb);
        void error(Exception e);
    }
}
