package dev.vortex.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/** A provisional GUI. It is not a recreation of an existing reference. */
public final class VortexScreen extends Screen {
    private TextFieldWidget search;
    private String query = "", group = "ALL";
    private int page;
    private int left, top, panelWidth, panelHeight, rowX, rowY, rowW, perPage;
    private static final String[] GROUPS = { "ALL", "HUD", "MOVEMENT", "PLAYER" };
    public VortexScreen() { super(Text.literal("Vortex Client")); }
    @Override protected void init() {
        panelWidth = Math.min(440, Math.max(230, width - 24));
        panelHeight = Math.min(330, Math.max(180, height - 24));
        left = (width - panelWidth) / 2; top = (height - panelHeight) / 2;
        rowX = left + 103; rowY = top + 74; rowW = panelWidth - 115;
        perPage = Math.max(1, (panelHeight - 108) / 31);
        search = new TextFieldWidget(textRenderer, rowX, top + 42, rowW, 20, Text.literal("Search modules"));
        search.setMaxLength(80);
        search.setText(query);
        search.setChangedListener(value -> { query = value; page = 0; });
        addDrawableChild(search);
    }
    private List<Modules.Module> filtered() {
        List<Modules.Module> out = new ArrayList<>();
        String q = query.toLowerCase(Locale.ROOT).trim();
        for (Modules.Module m : VortexClient.MODULES.all)
            if ((group.equals("ALL") || m.group.name().equals(group))
                    && (m.name.toLowerCase(Locale.ROOT).contains(q) || m.description.toLowerCase(Locale.ROOT).contains(q))) out.add(m);
        return out;
    }
    private static boolean in(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }
    private static void rounded(DrawContext c, int x, int y, int w, int h, int color) {
        c.fill(x + 3, y, x + w - 3, y + h, color);
        c.fill(x, y + 3, x + w, y + h - 3, color);
        c.fill(x + 1, y + 1, x + w - 1, y + h - 1, color);
    }
    @Override public void render(DrawContext c, int mouseX, int mouseY, float delta) {
        c.fill(0, 0, width, height, 0xA6000000);
        rounded(c, left, top, panelWidth, panelHeight, 0xEE251337);
        rounded(c, left + 1, top + 1, panelWidth - 2, panelHeight - 2, 0xF2130D1D);
        c.fill(left + 12, top + 30, left + panelWidth - 12, top + 31, 0xFF4E236D);
        c.drawText(textRenderer, "VORTEX", left + 13, top + 12, 0xFF5CF2FF, false);
        c.drawText(textRenderer, "CLIENT / EARLY BUILD", left + 67, top + 12, 0xFFB79BC7, false);
        for (int i = 0; i < GROUPS.length; i++) {
            int y = top + 45 + i * 26;
            boolean selected = group.equals(GROUPS[i]);
            if (selected) rounded(c, left + 10, y - 5, 82, 21, 0xFF39234D);
            c.drawText(textRenderer, GROUPS[i], left + 16, y, selected ? 0xFF64EFF9 : 0xFFAB9AB7, false);
        }
        List<Modules.Module> visible = filtered();
        int pages = Math.max(1, (visible.size() + perPage - 1) / perPage);
        page = Math.min(page, pages - 1);
        String tooltip = null;
        for (int i = 0; i < perPage && page * perPage + i < visible.size(); i++) {
            Modules.Module m = visible.get(page * perPage + i);
            int y = rowY + i * 31;
            boolean hovered = in(mouseX, mouseY, rowX, y, rowW, 26);
            rounded(c, rowX, y, rowW, 26, hovered ? 0xFF382247 : 0xFF251A30);
            c.fill(rowX + 3, y + 5, rowX + 5, y + 21, m.enabled ? 0xFF56E8F4 : 0xFF624C6C);
            c.drawText(textRenderer, m.name, rowX + 10, y + 9, 0xFFF1E9F5, false);
            c.drawText(textRenderer, m.enabled ? "ON" : "OFF", rowX + rowW - 24, y + 9, m.enabled ? 0xFF56E8F4 : 0xFF9E88A7, false);
            if (hovered) tooltip = m.description;
        }
        int footer = top + panelHeight - 22;
        c.drawText(textRenderer, "<", rowX + 3, footer, 0xFFBDA7C8, false);
        c.drawText(textRenderer, (page + 1) + " / " + pages, rowX + 17, footer, 0xFFBDA7C8, false);
        c.drawText(textRenderer, ">", rowX + 48, footer, 0xFFBDA7C8, false);
        c.drawText(textRenderer, "ESC to close", left + panelWidth - 77, footer, 0xFF89778F, false);
        super.render(c, mouseX, mouseY, delta);
        if (tooltip != null) c.drawTooltip(textRenderer, Text.literal(tooltip), mouseX, mouseY);
    }
    @Override public boolean mouseClicked(Click click, boolean doubled) {
        if (super.mouseClicked(click, doubled)) return true;
        if (click.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;
        double x = click.x(), y = click.y();
        for (int i = 0; i < GROUPS.length; i++) {
            if (in(x, y, left + 10, top + 40 + i * 26, 82, 21)) { group = GROUPS[i]; page = 0; return true; }
        }
        int footer = top + panelHeight - 22;
        if (in(x, y, rowX, footer - 3, 12, 16)) { page = Math.max(0, page - 1); return true; }
        if (in(x, y, rowX + 44, footer - 3, 16, 16)) {
            int maxPage = Math.max(0, (filtered().size() - 1) / perPage);
            page = Math.min(maxPage, page + 1); return true;
        }
        List<Modules.Module> visible = filtered();
        for (int i = 0; i < perPage && page * perPage + i < visible.size(); i++) {
            if (in(x, y, rowX, rowY + i * 31, rowW, 26)) {
                Modules.Module m = visible.get(page * perPage + i);
                m.enabled = !m.enabled;
                VortexClient.CONFIG.save(VortexClient.MODULES);
                return true;
            }
        }
        return false;
    }
    @Override public boolean shouldPause() { return false; }
}
