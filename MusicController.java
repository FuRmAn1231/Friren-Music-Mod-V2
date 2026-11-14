package com.frierenmusic;

import net.minecraft.client.Minecraft;

/** Decides which layer is wanted each tick and drives the fades. */
public final class MusicController {
    private MusicController() {}

    private static final Layer CALM = new Layer(Tracks::calm, true, Config.CALM_FADE_IN, Config.CALM_FADE_OUT);
    private static final Layer COMBAT = new Layer(Tracks::combat, false, Config.COMBAT_FADE_IN, Config.COMBAT_FADE_OUT);
    private static final Layer BOSS = new Layer(Tracks::boss, false, Config.BOSS_FADE_IN, Config.BOSS_FADE_OUT);

    private static final CombatTracker COMBAT_TRACKER = new CombatTracker();
    private static final BossDetector BOSS_DETECTOR = new BossDetector();

    private static boolean inWorld;
    private static int now;
    private static int bossTail;

    public static void tick(Minecraft mc) {
        if (mc.level == null || mc.player == null) {
            if (inWorld) reset(mc);
            return;
        }
        if (!inWorld) {
            inWorld = true;
            mc.getMusicManager().stopPlaying(); // silence vanilla music that was running in the menu
        }
        now++;

        COMBAT_TRACKER.tick(mc, now);
        BOSS_DETECTOR.tick(mc);

        if (BOSS_DETECTOR.present) {
            bossTail = Config.TAIL_TICKS;
            if (BOSS_DETECTOR.hint != null) BOSS.forceNext(BOSS_DETECTOR.hint);
        } else if (bossTail > 0) {
            bossTail--;
        }

        boolean bossWanted = BOSS_DETECTOR.present || bossTail > 0;
        boolean combatWanted = !bossWanted && COMBAT_TRACKER.wanted();
        boolean calmWanted = !bossWanted && !combatWanted;

        CALM.update(mc, calmWanted, now);
        COMBAT.update(mc, combatWanted, now);
        BOSS.update(mc, bossWanted, now);
    }

    public static void reset(Minecraft mc) {
        inWorld = false;
        bossTail = 0;
        COMBAT_TRACKER.reset();
        CALM.reset(mc);
        COMBAT.reset(mc);
        BOSS.reset(mc);
    }
}
