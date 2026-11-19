package dev.vortex.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/** Three-panel module desk inspired by the supplied Spectre screenshots. */
public final class VortexDashboard extends Screen {
    private static final int BG = 0xF20B0911, SIDEBAR = 0xFA120F1B;
    private static final int PANEL = 0xF5191425, CARD = 0xFF211B30;
    private static final int SELECTED = 0xFF302440, ACCENT = 0xFF55E6E9;
    private static final int TEXT = 0xFFECE9F4, MUTED = 0xFFAD9FB9;
    private static final int BORDER = 0xFF483651;
    private static final String[] GROUPS = {"ALL", "HUD", "MOVEMENT", "PLAYER", "INVENTORY"};
    private TextFieldWidget search;
    private String query = "", group = "ALL";
    private Modules.Module selected = VortexClient.MODULES.all.get(0);
    private int page;
    private int x, y, w, h, leftW, mainX, mainW, detailX, detailW, footerY;
    private int cardsY, cardW, cols, rows, perPage, toggleY;

    public VortexDashboard() { super(Text.literal("Vortex Client")); }
    @Override protected void init() {
        w = Math.min(800, width - 16);
        h = Math.min(470, height - 16);
        x = (width - w) / 2; y = (height - h) / 2;
        leftW = Math.max(65, Math.min(122, w / 6));
        detailW = Math.max(92, w / 4);
        mainX = x + leftW + 12;
        detailX = x + w - detailW - 6;
        mainW = detailX - mainX - 8;
        footerY = y + h - 18;
        cardsY = y + 100;
        cols = mainW >= 170 ? 2 : 1;
        cardW = (mainW - (cols - 1) * 6) / cols;
        rows = Math.max(1, (footerY - cardsY - 4) / 37);
        perPage = rows * cols;
        toggleY = footerY - 37;
        search = new TextFieldWidget(textRenderer, mainX, y + 75, mainW, 18, Text.literal("Search modules"));
        search.setMaxLength(80);
        search.setPlaceholder(Text.literal("Search modules..."));
        search.setText(query);
        search.setChangedListener(value -> { query = value; page = 0; });
        addDrawableChild(search);
    }
    private List<Modules.Module> filtered() {
        List<Modules.Module> list = new ArrayList<>();
        String q = query.trim().toLowerCase(Locale.ROOT);
        for (Modules.Module m : VortexClient.MODULES.all) {
            if ((group.equals("ALL") || m.group.name().equals(group))
                    && (m.name.toLowerCase(Locale.ROOT).contains(q)
                    || m.description.toLowerCase(Locale.ROOT).contains(q))) list.add(m);
        }
        return list;
    }
    private static boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }
    private static void rounded(DrawContext c, int x, int y, int w, int h, int color) {
        if (w < 4 || h < 4) return;
        c.fill(x + 2, y, x + w - 2, y + h, color);
        c.fill(x, y + 2, x + w, y + h - 2, color);
        c.fill(x + 1, y + 1, x + w - 1, y + h - 1, color);
    }
    private void label(DrawContext c, String value, int x, int y, int maxWidth, int color) {
        c.drawText(textRenderer, textRenderer.trimToWidth(value, Math.max(0, maxWidth)), x, y, color, false);
    }
    private String categoryName() {
        return switch (group) {
            case "HUD" -> "HUD";
            case "MOVEMENT" -> "Movement";
            case "PLAYER" -> "Player";
            case "INVENTORY" -> "Inventory";
            default -> "All modules";
        };
    }
    private void switchMark(DrawContext c, int sx, int sy, boolean active) {
        rounded(c, sx, sy, 20, 10, active ? ACCENT : 0xFF4C4656);
        rounded(c, sx + (active ? 11 : 1), sy + 1, 8, 8, 0xFFF4F3F8);
    }
    private void toggle(Modules.Module module) {
        module.enabled = !module.enabled;
        VortexClient.CONFIG.save(VortexClient.MODULES);
    }
    @Override public void render(DrawContext c, int mouseX, int mouseY, float delta) {
        c.fill(0, 0, width, height, 0xA9000000);
        rounded(c, x - 1, y - 1, w + 2, h + 2, BORDER);
        rounded(c, x, y, w, h, BG);
        c.fill(x, y + 28, x + w, y + 29, BORDER);
        c.fill(x, footerY, x + w, footerY + 1, BORDER);
        rounded(c, x + 6, y + 33, leftW - 4, footerY - y - 35, SIDEBAR);
        // Vortex's compact V mark, replacing the reference's Spectre ghost.
        rounded(c, x + 9, y + 7, 15, 15, 0xFF1A343A);
        c.drawText(textRenderer, "V", x + 13, y + 10, ACCENT, false);
        c.drawText(textRenderer, "VORTEX", x + 29, y + 6, TEXT, false);
        c.drawText(textRenderer, "MODULE CONTROL", x + 29, y + 17, MUTED, false);
        label(c, "MODULES", x + w - 64, y + 10, 50, ACCENT);
        c.fill(x + w - 64, y + 25, x + w - 18, y + 26, ACCENT);
        label(c, "WORKSPACE", x + 12, y + 40, leftW - 18, MUTED);
        for (int i = 0; i < GROUPS.length; i++) {
            int gy = y + 57 + i * 22;
            boolean active = group.equals(GROUPS[i]);
            if (active) {
                rounded(c, x + 8, gy - 4, leftW - 8, 19, 0xFF21303A);
                c.fill(x + 8, gy - 3, x + 10, gy + 14, ACCENT);
            }
            c.fill(x + 14, gy + 3, x + 18, gy + 7, active ? ACCENT : MUTED);
            String title = switch (GROUPS[i]) {
                case "ALL" -> "All modules";
                case "HUD" -> "HUD";
                case "MOVEMENT" -> "Movement";
                case "PLAYER" -> "Player";
                default -> "Inventory";
            };
            if (leftW < 75 && title.equals("All modules")) title = "All";
            if (leftW < 75 && title.equals("Movement")) title = "Move";
            label(c, title, x + 22, gy, leftW - 25, active ? TEXT : MUTED);
        }
        rounded(c, mainX, y + 34, mainW, 35, 0xFF201D31);
        c.fill(mainX + 3, y + 38, mainX + 5, y + 65, ACCENT);
        label(c, "YOUR MODULE DESK", mainX + 10, y + 39, mainW - 18, ACCENT);
        label(c, categoryName(), mainX + 10, y + 53, mainW - 18, TEXT);
        rounded(c, detailX, y + 34, detailW, footerY - y - 39, BORDER);
        rounded(c, detailX + 1, y + 35, detailW - 2, footerY - y - 41, PANEL);
        label(c, "MODULE SETTINGS", detailX + 8, y + 43, detailW - 16, MUTED);
        List<Modules.Module> visible = filtered();
        int pages = Math.max(1, (visible.size() + perPage - 1) / perPage);
        page = Math.min(page, pages - 1);
        String tooltip = null;
        for (int i = 0; i < perPage && page * perPage + i < visible.size(); i++) {
            Modules.Module m = visible.get(page * perPage + i);
            int cx = mainX + (i % cols) * (cardW + 6);
            int cy = cardsY + (i / cols) * 37;
            boolean hover = inside(mouseX, mouseY, cx, cy, cardW, 32);
            rounded(c, cx, cy, cardW, 32, m == selected ? ACCENT : BORDER);
            rounded(c, cx + 1, cy + 1, cardW - 2, 30, m == selected ? SELECTED : CARD);
            label(c, m.name, cx + 6, cy + 6, cardW - 34, TEXT);
            label(c, m.enabled ? "ACTIVE" : "INACTIVE", cx + 6, cy + 19, cardW - 34, m.enabled ? ACCENT : MUTED);
            switchMark(c, cx + cardW - 25, cy + 11, m.enabled);
            if (hover) tooltip = m.name + ": " + m.description;
        }
        if (visible.isEmpty()) label(c, "No matching modules", mainX + 5, cardsY + 8, mainW - 10, MUTED);
        if (selected == null) {
            label(c, "Select a module", detailX + 8, y + 61, detailW - 16, TEXT);
        } else {
            label(c, selected.name, detailX + 8, y + 61, detailW - 16, TEXT);
            label(c, selected.group.name(), detailX + 8, y + 75, detailW - 16, MUTED);
            int textY = y + 93;
            for (OrderedText line : textRenderer.wrapLines(Text.literal(selected.description), detailW - 16)) {
                if (textY + 10 >= toggleY - 9) break;
                c.drawText(textRenderer, line, detailX + 8, textY, TEXT, false);
                textY += 11;
            }
            c.fill(detailX + 8, toggleY - 7, detailX + detailW - 8, toggleY - 6, BORDER);
            rounded(c, detailX + 8, toggleY, detailW - 16, 19, selected.enabled ? 0xFF225457 : 0xFF34303C);
            label(c, selected.enabled ? "Disable module" : "Enable module", detailX + 13, toggleY + 6, detailW - 26, TEXT);
        }
        label(c, "VORTEX", x + 10, footerY + 5, leftW - 10, MUTED);
        c.drawText(textRenderer, "<", mainX + 3, footerY + 5, ACCENT, false);
        c.drawText(textRenderer, (page + 1) + " / " + pages, mainX + 17, footerY + 5, MUTED, false);
        c.drawText(textRenderer, ">", mainX + 48, footerY + 5, ACCENT, false);
        long active = VortexClient.MODULES.all.stream().filter(m -> m.enabled).count();
        label(c, active + " active", detailX + 8, footerY + 5, detailW - 14, MUTED);
        super.render(c, mouseX, mouseY, delta);
        if (tooltip != null) c.drawTooltip(textRenderer, Text.literal(tooltip), mouseX, mouseY);
    }
    @Override public boolean mouseClicked(Click click, boolean doubled) {
        if (super.mouseClicked(click, doubled)) return true;
        double mx = click.x(), my = click.y();
        if (click.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            for (int i = 0; i < GROUPS.length; i++) {
                if (inside(mx, my, x + 8, y + 53 + i * 22, leftW - 8, 19)) {
                    group = GROUPS[i]; page = 0; return true;
                }
            }
            if (inside(mx, my, mainX, footerY + 1, 12, 17)) { page = Math.max(0, page - 1); return true; }
            if (inside(mx, my, mainX + 44, footerY + 1, 16, 17)) {
                page = Math.min(Math.max(0, (filtered().size() - 1) / perPage), page + 1); return true;
            }
            if (selected != null && inside(mx, my, detailX + 8, toggleY, detailW - 16, 19)) {
                toggle(selected); return true;
            }
        }
        if (click.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT && click.button() != GLFW.GLFW_MOUSE_BUTTON_RIGHT) return false;
        List<Modules.Module> list = filtered();
        for (int i = 0; i < perPage && page * perPage + i < list.size(); i++) {
            int cx = mainX + (i % cols) * (cardW + 6), cy = cardsY + (i / cols) * 37;
            if (inside(mx, my, cx, cy, cardW, 32)) {
                Modules.Module module = list.get(page * perPage + i);
                selected = module;
                if (click.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT && inside(mx, my, cx + cardW - 29, cy, 29, 32)) toggle(module);
                return true;
            }
        }
        return false;
    }
    @Override public boolean shouldPause() { return false; }
}
