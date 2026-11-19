package dev.vortex.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

/** Small local config. Failed or corrupt loads never silently overwrite the original file. */
public final class VortexConfig {
    private final Path file;
    private boolean writable = true;
    public VortexConfig(Path file) { this.file = file; }
    public boolean load(Modules modules) {
        if (!Files.exists(file)) return true;
        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(file)) {
            props.load(in);
            for (Modules.Module m : modules.all) {
                String value = props.getProperty("module." + m.id);
                if ("true".equalsIgnoreCase(value)) m.enabled = true;
                else if ("false".equalsIgnoreCase(value)) m.enabled = false;
            }
            return true;
        } catch (IOException | IllegalArgumentException ex) {
            writable = false;
            System.err.println("[Vortex] Could not read config. Preserving the original file: " + ex);
            return false;
        }
    }
    public boolean save(Modules modules) {
        if (!writable) return false;
        Path tmp = null;
        try {
            Files.createDirectories(file.getParent());
            tmp = Files.createTempFile(file.getParent(), "vortex-", ".tmp");
            Properties props = new Properties();
            for (Modules.Module m : modules.all) props.setProperty("module." + m.id, Boolean.toString(m.enabled));
            try (OutputStream out = Files.newOutputStream(tmp)) { props.store(out, "Vortex Client settings"); }
            try { Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE); }
            catch (AtomicMoveNotSupportedException ex) { Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING); }
            return true;
        } catch (IOException | SecurityException ex) {
            System.err.println("[Vortex] Could not save config: " + ex);
            return false;
        } finally {
            if (tmp != null) try { Files.deleteIfExists(tmp); } catch (IOException ignored) { }
        }
    }
}
