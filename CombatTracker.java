package com.frierenmusic;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Client-side combat detection.
 *  - the player gets hurt by a hostile / neutral mob or another player  -> combat
 *  - the player hurts a hostile / neutral mob or another player (melee, arrows, tridents...) -> combat
 * Combat ends: 15 s with no such events, the player dies, or the engaged enemies are killed.
 * After that the combat music plays 5 more seconds (the "tail") and then fades out.
 */
public final class CombatTracker {
    private boolean active;
    private int tail;
    private int lastEvent;
    private boolean wasDead;
    /** entity id -> tick of last combat event with it */
    private final Map<Integer, Integer> engaged = new HashMap<>();

    public boolean wanted() { return active || tail > 0; }

    public void reset() {
        active = false;
        tail = 0;
        wasDead = false;
        engaged.clear();
    }

    public void tick(Minecraft mc, int now) {
        LocalPlayer p = mc.player;
        if (p == null || mc.level == null) return;

        if (p.isDeadOrDying()) {
            if (!wasDead) { wasDead = true; end(); }
            if (tail > 0) tail--;
            return;
        }
        wasDead = false;

        // 1) damage received
        if (p.hurtTime > 0) {
            DamageSource src = p.getLastDamageSource();
            if (src != null) {
                Entity attacker = src.getEntity();
                if (isThreat(attacker, p)) hit(attacker, now);
            }
        }

        // 2) damage dealt (every 2 ticks is enough, hurtTime lasts 10 ticks)
        if ((now & 1) == 0) {
            var box = p.getBoundingBox().inflate(Config.SCAN_RADIUS);
            for (LivingEntity e : mc.level.getEntitiesOfClass(LivingEntity.class, box)) {
                if (e == p || e.hurtTime <= 0 || !isThreat(e, p)) continue;
                DamageSource src = e.getLastDamageSource();
                if (src != null && src.getEntity() == p) hit(e, now);
            }
        }

        // 3) bookkeeping: kills, despawns, stale entries
        boolean killed = false;
        for (Iterator<Map.Entry<Integer, Integer>> it = engaged.entrySet().iterator(); it.hasNext(); ) {
            var en = it.next();
            Entity e = mc.level.getEntity(en.getKey());
            if (e == null || e.isRemoved()) {
                it.remove();
            } else if (e instanceof LivingEntity le && le.isDeadOrDying()) {
                it.remove();
                killed = true;
            } else if (now - en.getValue() > Config.COMBAT_TIMEOUT_TICKS) {
                it.remove();
            }
        }

        if (active) {
            if (killed && engaged.isEmpty()) end();
            else if (now - lastEvent >= Config.COMBAT_TIMEOUT_TICKS) end();
        } else if (tail > 0) {
            tail--;
        }
    }

    private void hit(Entity e, int now) {
        engaged.put(e.getId(), now);
        lastEvent = now;
        active = true;
        tail = 0;
    }

    private void end() {
        if (active) tail = Config.TAIL_TICKS;
        active = false;
        engaged.clear();
    }

    /** Hostile mobs, neutral mobs (wolves, endermen, bees, piglins...) and other players. Tamed pets excluded. */
    static boolean isThreat(Entity e, Player self) {
        if (e == null || e == self) return false;
        if (e instanceof Player) return true;
        if (e instanceof TamableAnimal t && t.isTame()) return false;
        return e instanceof Enemy || e instanceof NeutralMob;
    }
}
