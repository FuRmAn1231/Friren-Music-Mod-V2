package com.frierenmusic;

import com.frierenmusic.mixin.BossHealthOverlayAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.network.chat.contents.TranslatableContents;

import java.util.Set;

/**
 * A boss is anything that shows a boss bar at the top of the screen - vanilla or from other mods.
 * Not counted: raid bars, and Warden / Elder Guardian (in case a mod gives them a bar).
 */
public final class BossDetector {
    private static final Set<String> IGNORED_KEYS = Set.of(
            "entity.minecraft.warden",
            "entity.minecraft.elder_guardian");

    public boolean present;
    /** Preferred theme for the current boss (e.g. Ender Dragon), or null. */
    public String hint;

    public void tick(Minecraft mc) {
        present = false;
        hint = null;
        if (mc.player == null || mc.player.isDeadOrDying()) return;

        var events = ((BossHealthOverlayAccessor) mc.gui.getBossOverlay()).frierenmusic$getEvents();
        for (LerpingBossEvent ev : events.values()) {
            String key = null;
            if (ev.getName().getContents() instanceof TranslatableContents tc) key = tc.getKey();
            if (key != null) {
                if (key.startsWith("event.minecraft.raid")) continue;
                if (IGNORED_KEYS.contains(key)) continue;
                if (key.equals("entity.minecraft.ender_dragon")) hint = Tracks.DRAGON_THEME;
            }
            present = true;
        }
    }
}
