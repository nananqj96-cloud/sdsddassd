package dev.vortex.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

/** Independent utility implementations, not imported Corz code. Both are opt-in. */
public final class UtilityFeatures {
    private Object weatherWorld;
    private int previousWeather = -1;

    public void tick(MinecraftClient client, Modules modules) {
        if (client.player == null || client.world == null) {
            weatherWorld = null;
            previousWeather = -1;
            return;
        }
        if (modules.enabled("auto_tool") && client.currentScreen == null
                && client.options.attackKey.isPressed()
                && client.crosshairTarget instanceof BlockHitResult hit
                && hit.getType() == HitResult.Type.BLOCK) {
            var inventory = client.player.getInventory();
            var state = client.world.getBlockState(hit.getBlockPos());
            if (!state.isAir() && state.getHardness(client.world, hit.getBlockPos()) >= 0) {
                float[] speeds = new float[9];
                for (int slot = 0; slot < 9; slot++) {
                    speeds[slot] = inventory.getStack(slot).getMiningSpeedMultiplier(state);
                }
                int best = ToolSelection.bestSlot(inventory.getSelectedSlot(), speeds);
                if (best != inventory.getSelectedSlot()) inventory.setSelectedSlot(best);
            }
        }
        if (!modules.enabled("weather_notifier")) {
            weatherWorld = null;
            previousWeather = -1;
            return;
        }
        int weather = client.world.isThundering() ? 2 : client.world.isRaining() ? 1 : 0;
        if (weatherWorld == client.world && previousWeather >= 0 && weather != previousWeather) {
            String label = weather == 2 ? "Thunderstorm" : weather == 1 ? "Rain" : "Clear";
            client.player.sendMessage(Text.literal("[Vortex] Weather changed: " + label), false);
        }
        weatherWorld = client.world;
        previousWeather = weather;
    }
}
