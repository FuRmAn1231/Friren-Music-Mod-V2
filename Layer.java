package com.frierenmusic;

import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.function.Supplier;

/**
 * One music layer (calm / combat / boss) with its own fade level.
 *
 * gapped == true (calm): the track keeps playing silently while another layer is on top,
 *   so when calm fades back in it continues where it was. Between tracks there is a pause.
 * gapped == false (combat, boss): a track starts when the layer is wanted and is stopped
 *   once it has faded to 0. When a track ends the next one follows immediately.
 */
public final class Layer {
    private final Supplier<List<String>> source;
    private final boolean gapped;
    private final int fadeIn, fadeOut;

    public float gain = 0f;
    private LayerSound current;
    private int startedAt;
    private int gap = 0;
    private String last;
    private String forcedNext;
    private final Deque<String> bag = new ArrayDeque<>();

    public Layer(Supplier<List<String>> source, boolean gapped, int fadeIn, int fadeOut) {
        this.source = source;
        this.gapped = gapped;
        this.fadeIn = fadeIn;
        this.fadeOut = fadeOut;
    }

    /** Next track to start will be this one (if it exists in the track list). */
    public void forceNext(String name) { this.forcedNext = name; }

    public void update(Minecraft mc, boolean wanted, int now) {
        gain = Math.max(0f, Math.min(1f, gain + (wanted ? 1f / fadeIn : -1f / fadeOut)));
        SoundManager sm = mc.getSoundManager();

        if (current != null && now - startedAt > 10 && !sm.isActive(current)) {
            current = null; // track finished
            if (gapped) gap = Config.CALM_GAP_TICKS;
        }

        if (gapped) {
            if (current == null) {
                if (gap > 0) gap--; else start(sm, now);
            }
        } else {
            if (wanted && current == null) start(sm, now);
            if (!wanted && gain <= 0f && current != null) stop(sm);
        }
    }

    public void reset(Minecraft mc) {
        if (current != null) stop(mc.getSoundManager());
        gain = 0f;
        gap = 0;
        forcedNext = null;
    }

    private void stop(SoundManager sm) {
        sm.stop(current);
        current = null;
    }

    private void start(SoundManager sm, int now) {
        String name = pick();
        if (name == null) return;
        SoundEvent ev = SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(Tracks.NAMESPACE, name));
        current = new LayerSound(ev, this);
        startedAt = now;
        sm.play(current);
        last = name;
    }

    private String pick() {
        List<String> all = source.get();
        if (all.isEmpty()) return null;
        if (forcedNext != null) {
            String f = forcedNext;
            forcedNext = null;
            if (all.contains(f)) return f;
        }
        if (bag.isEmpty()) {
            List<String> shuffled = new ArrayList<>(all);
            Collections.shuffle(shuffled);
            if (shuffled.size() > 1 && shuffled.get(0).equals(last)) {
                Collections.swap(shuffled, 0, shuffled.size() - 1);
            }
            bag.addAll(shuffled);
        }
        return bag.poll();
    }
}
