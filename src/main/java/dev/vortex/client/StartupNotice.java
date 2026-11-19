package dev.vortex.client;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.screen.TitleScreen;
import org.slf4j.LoggerFactory;

/** Visible on every title-screen visit; the CI path is opt-in and never joins a server. */
public final class StartupNotice {
    public static final String MESSAGE = "Vortex Client - For Anarchy Servers";
    private static int titleFrames, dashboardFrames;
    private static boolean opened;

    private StartupNotice() { }

    public static void register() {
        LoggerFactory.getLogger("Vortex").info(MESSAGE);
        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            ScreenEvents.afterRender(screen).register((rendered, context, mouseX, mouseY, delta) -> {
                if (rendered instanceof TitleScreen) {
                    context.fill(0, 0, rendered.width, 23, 0xD0100C1C);
                    context.drawCenteredTextWithShadow(client.textRenderer, MESSAGE,
                            rendered.width / 2, 8, 0xFF55E6E9);
                    if (Boolean.getBoolean("vortex.ciSmoke") && !opened && ++titleFrames >= 30) {
                        opened = true;
                        client.execute(() -> client.setScreen(new VortexDashboard()));
                    }
                } else if (Boolean.getBoolean("vortex.ciSmoke") && opened
                        && rendered instanceof VortexDashboard && ++dashboardFrames >= 30) {
                    LoggerFactory.getLogger("Vortex").info("VORTEX_CI_GUI_SMOKE_PASS");
                    client.scheduleStop();
                }
            });
        });
    }
}
