import dev.vortex.client.Modules;
import dev.vortex.client.VortexConfig;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigSmokeTest {
    private static void check(boolean x, String message) { if (!x) throw new AssertionError(message); }
    public static void main(String[] args) throws Exception {
        Path dir = Files.createTempDirectory("vortex-test");
        Path path = dir.resolve("vortex.properties");
        Modules initial = new Modules();
        check(initial.enabled("fps"), "default FPS");
        check(!initial.enabled("respawn"), "safe respawn default");
        initial.all.stream().filter(m -> m.id.equals("sprint")).findFirst().orElseThrow().enabled = true;
        check(new VortexConfig(path).save(initial), "save succeeds");
        Modules loaded = new Modules();
        check(new VortexConfig(path).load(loaded), "load succeeds");
        check(loaded.enabled("sprint") && loaded.enabled("fps"), "saved values round-trip");
        Files.writeString(path, "bad=\\uXX00\n");
        VortexConfig corrupt = new VortexConfig(path);
        check(!corrupt.load(loaded), "corrupt config detected");
        check(!corrupt.save(loaded), "corrupt file is protected");
        check(Files.readString(path).equals("bad=\\uXX00\n"), "original stays intact");
        System.out.println("PASS: defaults, save/load, corrupt-file protection");
    }
}
