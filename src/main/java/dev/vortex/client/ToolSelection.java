package dev.vortex.client;

/** Preserve the selected slot on ties; switch only for a strictly faster hotbar item. */
public final class ToolSelection {
    private ToolSelection() { }

    public static int bestSlot(int selected, float[] speeds) {
        if (speeds.length != 9 || selected < 0 || selected >= 9)
            throw new IllegalArgumentException("Expected nine hotbar speeds and a valid selected slot");
        int best = selected;
        float bestSpeed = Float.isFinite(speeds[selected]) ? speeds[selected] : 0;
        for (int slot = 0; slot < speeds.length; slot++) {
            if (Float.isFinite(speeds[slot]) && speeds[slot] > bestSpeed) {
                best = slot;
                bestSpeed = speeds[slot];
            }
        }
        return best;
    }
}
