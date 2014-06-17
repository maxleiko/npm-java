package fr.braindead.npmjava;

import com.google.gson.JsonObject;
import fr.braindead.npmjava.command.NpmView;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by leiko on 17/06/14.
 */
public class TestNpmView {

    private NpmView cmd;

    @Before
    public void setup() {
        cmd = new NpmView();
    }

    @Test()
    public void testExecute() {
        try {
            JsonObject view = cmd.execute("kevoree-chan-websocket");
            org.junit.Assert.assertNotNull("should not be null", view.get("version"));
            org.junit.Assert.assertNotNull("should not be null", view.get("versions"));

        } catch (Exception e) {
            e.getMessage();
        }
    }
}
