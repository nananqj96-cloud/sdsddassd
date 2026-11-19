package dev.vortex.client;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.util.Identifier;
import net.minecraft.item.Items;
import org.lwjgl.glfw.GLFW;

public final class VortexClient implements ClientModInitializer {
    public static final Modules MODULES = new Modules();
    public static VortexConfig CONFIG;
    private KeyBinding openGui;
    private final UtilityFeatures utilities = new UtilityFeatures();
    private Object previousWorld;
    private boolean speedReady;
    private double lastX, lastZ, speed;
    private long sessionStartedAt;
    private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Override public void onInitializeClient() {
        StartupNotice.register();
        CONFIG = new VortexConfig(FabricLoader.getInstance().getConfigDir().resolve("vortexclient.properties"));
        CONFIG.load(MODULES);
        sessionStartedAt = System.currentTimeMillis();
        KeyBinding.Category category = KeyBinding.Category.create(Identifier.of("vortexclient", "controls"));
        openGui = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.vortexclient.open_gui", GLFW.GLFW_KEY_RIGHT_SHIFT, category));
        ClientTickEvents.END_CLIENT_TICK.register(this::tick);
        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, Identifier.of("vortexclient", "status"), (context, delta) -> renderHud(context));
    }
    private void tick(MinecraftClient client) {
        utilities.tick(client, MODULES);
        while (openGui.wasPressed()) {
            if (client.currentScreen == null && client.player != null) client.setScreen(new VortexDashboard());
        }
        if (client.player == null || client.world == null) {
            previousWorld = null; speedReady = false; speed = 0; return;
        }
        if (previousWorld != client.world) { previousWorld = client.world; speedReady = false; }
        double x = client.player.getX(), z = client.player.getZ();
        if (speedReady) speed = Math.hypot(x - lastX, z - lastZ) * 20.0;
        lastX = x; lastZ = z; speedReady = true;
        if (MODULES.enabled("sprint") && client.currentScreen == null
                && client.options.forwardKey.isPressed() && !client.player.isSneaking()
                && !client.player.horizontalCollision && client.player.getHungerManager().getFoodLevel() > 6) {
            client.player.setSprinting(true);
        }
        if (MODULES.enabled("respawn") && client.currentScreen instanceof DeathScreen && client.player.isDead()) {
            client.player.requestRespawn();
            client.setScreen(null);
        }
    }
    private void renderHud(DrawContext context) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;
        List<String> lines = new ArrayList<>();
        if (MODULES.enabled("fps")) lines.add("FPS  " + mc.getCurrentFps());
        if (MODULES.enabled("ping")) {
            PlayerListEntry entry = mc.getNetworkHandler() == null ? null : mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
            lines.add("PING  " + (entry == null ? "--" : entry.getLatency() + " ms"));
        }
        if (MODULES.enabled("coordinates")) lines.add(String.format(Locale.ROOT, "XYZ  %.1f  %.1f  %.1f", mc.player.getX(), mc.player.getY(), mc.player.getZ()));
        if (MODULES.enabled("direction")) lines.add("FACING  " + mc.player.getHorizontalFacing().asString().toUpperCase(Locale.ROOT));
        if (MODULES.enabled("clock")) lines.add("TIME  " + CLOCK.format(LocalTime.now()));
        if (MODULES.enabled("speed")) lines.add(String.format(Locale.ROOT, "SPEED  %.2f b/s", speed));
        if (MODULES.enabled("world_time")) {
            long ticks = Math.floorMod(mc.world.getTimeOfDay(), 24000L);
            int hour = (int) ((ticks / 1000L + 6L) % 24L);
            int minute = (int) ((ticks % 1000L) * 60L / 1000L);
            lines.add(String.format(Locale.ROOT, "WORLD TIME  %02d:%02d", hour, minute));
        }
        if (MODULES.enabled("biome")) {
            String biome = mc.world.getBiome(mc.player.getBlockPos()).getKey()
                    .map(key -> key.getValue().toString()).orElse("unknown");
            lines.add("BIOME  " + biome.replace('_', ' '));
        }
        if (MODULES.enabled("sprint_status")) lines.add("SPRINT  " + (mc.player.isSprinting() ? "ON" : "OFF"));
        if (MODULES.enabled("session")) {
            long elapsed = Math.max(0L, (System.currentTimeMillis() - sessionStartedAt) / 1000L);
            long hours = elapsed / 3600L, minutes = (elapsed % 3600L) / 60L, seconds = elapsed % 60L;
            lines.add(String.format(Locale.ROOT, "SESSION  %02d:%02d:%02d", hours, minutes, seconds));
        }
        if (MODULES.enabled("memory")) {
            Runtime runtime = Runtime.getRuntime();
            long usedMb = (runtime.totalMemory() - runtime.freeMemory()) / (1024L * 1024L);
            long maxMb = runtime.maxMemory() / (1024L * 1024L);
            lines.add("MEMORY  " + usedMb + " / " + maxMb + " MB");
        }
        if (MODULES.enabled("server")) {
            var server = mc.getCurrentServerEntry();
            lines.add("SERVER  " + (server == null ? "Singleplayer" : server.name));
        }
        if (MODULES.enabled("health")) {
            lines.add(String.format(Locale.ROOT, "HEALTH  %.1f / %.1f", mc.player.getHealth(), mc.player.getMaxHealth()));
        }
        if (MODULES.enabled("armor")) {
            int durabilityLeft = 0, durabilityMax = 0;
            for (var slot : new net.minecraft.entity.EquipmentSlot[] {
                    net.minecraft.entity.EquipmentSlot.HEAD, net.minecraft.entity.EquipmentSlot.CHEST,
                    net.minecraft.entity.EquipmentSlot.LEGS, net.minecraft.entity.EquipmentSlot.FEET}) {
                var stack = mc.player.getEquippedStack(slot);
                if (!stack.isEmpty() && stack.isDamageable()) {
                    durabilityLeft += Math.max(0, stack.getMaxDamage() - stack.getDamage());
                    durabilityMax += stack.getMaxDamage();
                }
            }
            lines.add("ARMOR  " + mc.player.getArmor() + " pts  |  " + durabilityLeft + "/" + durabilityMax + " durability");
        }
        if (MODULES.enabled("inventory")) {
            int occupied = 0;
            for (int slot = 0; slot < 36; slot++) if (!mc.player.getInventory().getStack(slot).isEmpty()) occupied++;
            lines.add("INVENTORY  " + occupied + " / " + 36 + " slots");
        }
        if (MODULES.enabled("totem_count")) {
            int totems = 0;
            for (int slot = 0; slot < 36; slot++) {
                var stack = mc.player.getInventory().getStack(slot);
                if (stack.isOf(Items.TOTEM_OF_UNDYING)) totems += stack.getCount();
            }
            var offhand = mc.player.getOffHandStack();
            if (offhand.isOf(Items.TOTEM_OF_UNDYING)) totems += offhand.getCount();
            lines.add("TOTEMS  " + totems);
        }
        if (lines.isEmpty()) return;
        int width = mc.textRenderer.getWidth("VORTEX") + 20;
        for (String line : lines) width = Math.max(width, mc.textRenderer.getWidth(line) + 20);
        int height = 22 + 12 * lines.size();
        context.fill(8, 8, 8 + width, 8 + height, 0xC81A1029);
        context.fill(8, 8, 10, 8 + height, 0xFF37E7F5);
        context.drawText(mc.textRenderer, "VORTEX", 16, 14, 0xFF54EDFA, false);
        int y = 28;
        for (String line : lines) { context.drawText(mc.textRenderer, line, 16, y, 0xFFF0EAF8, false); y += 12; }
    }
}
