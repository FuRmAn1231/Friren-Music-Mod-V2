package com.frierenmusic;

/** All timings in one place. 20 ticks = 1 second. */
public final class Config {
    private Config() {}

    /** Combat ends after this long without any damage dealt/received. */
    public static final int COMBAT_TIMEOUT_TICKS = 15 * 20;
    /** After combat/boss ends, the battle music keeps playing this long before fading. */
    public static final int TAIL_TICKS = 5 * 20;
    /** Silence between two calm tracks. */
    public static final int CALM_GAP_TICKS = 90 * 20;

    // Fade durations (ticks) for each layer: how long 0 -> 100% and 100% -> 0 take.
    public static final int CALM_FADE_IN = 5 * 20;
    public static final int CALM_FADE_OUT = 3 * 20;
    public static final int COMBAT_FADE_IN = 2 * 20;
    public static final int COMBAT_FADE_OUT = 5 * 20;
    public static final int BOSS_FADE_IN = 3 * 20;
    public static final int BOSS_FADE_OUT = 5 * 20;

    /** Radius (blocks) in which we look for entities hurt by the player. */
    public static final double SCAN_RADIUS = 64.0;
}
