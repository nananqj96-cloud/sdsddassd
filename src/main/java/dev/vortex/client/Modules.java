package dev.vortex.client;

import java.util.List;

/** Only features with an implementation are registered here. */
public final class Modules {
    public enum Group { HUD, MOVEMENT, PLAYER, INVENTORY }
    public static final class Module {
        public final String id, name, description;
        public final Group group;
        public boolean enabled;
        Module(String id, String name, String description, Group group, boolean enabled) {
            this.id = id; this.name = name; this.description = description;
            this.group = group; this.enabled = enabled;
        }
    }
    public final List<Module> all = List.of(
        new Module("fps", "FPS Counter", "Show current frames per second.", Group.HUD, true),
        new Module("ping", "Ping Display", "Show your server latency when available.", Group.HUD, true),
        new Module("coordinates", "Coordinates", "Show your current X, Y and Z.", Group.HUD, true),
        new Module("direction", "Direction HUD", "Show the direction you are facing.", Group.HUD, true),
        new Module("clock", "Clock", "Show your computer's local time.", Group.HUD, false),
        new Module("speed", "Speed Display", "Show horizontal blocks per second, sampled each tick.", Group.HUD, false),
        new Module("world_time", "World Time", "Show the in-game time of day.", Group.HUD, false),
        new Module("biome", "Biome Display", "Show the biome at your current position.", Group.HUD, false),
        new Module("sprint_status", "Sprint Indicator", "Show whether your player is sprinting.", Group.HUD, false),
        new Module("session", "Session Timer", "Show time since this client session started.", Group.HUD, false),
        new Module("memory", "Memory Usage", "Show JVM heap usage for Minecraft.", Group.HUD, false),
        new Module("server", "Server Information", "Show the current server name when available.", Group.HUD, false),
        new Module("health", "Health Display", "Show current and maximum health.", Group.HUD, false),
        new Module("armor", "Armor Status", "Show armor points and equipped armor durability.", Group.HUD, false),
        new Module("inventory", "Inventory Slots", "Show how many main inventory slots are occupied.", Group.INVENTORY, false),
        new Module("totem_count", "Totem Counter", "Count totems in your main inventory, hotbar and offhand.", Group.INVENTORY, false),
        new Module("sprint", "Sprint", "Sprint while moving forward when allowed by the game.", Group.MOVEMENT, false),
        new Module("respawn", "Auto Respawn", "Immediately request respawn on the death screen.", Group.PLAYER, false),
        new Module("auto_tool", "Auto Tool", "Select a faster hotbar tool while mining. Does not restore the previous slot.", Group.INVENTORY, false),
        new Module("weather_notifier", "Weather Notifier", "Local chat notice when rain or thunder changes. No server messages.", Group.PLAYER, false)
    );
    public boolean enabled(String id) {
        return all.stream().anyMatch(module -> module.id.equals(id) && module.enabled);
    }
}
