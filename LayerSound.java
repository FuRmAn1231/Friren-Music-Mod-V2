package com.frierenmusic;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

/**
 * A music track whose volume follows its {@link Layer}'s fade value every tick.
 * Category is MUSIC, so the in-game "Music" slider scales it linearly (100% = 1.0, 75% = 0.75 ...).
 */
public class LayerSound extends AbstractTickableSoundInstance {
    private final Layer layer;

    public LayerSound(SoundEvent event, Layer layer) {
        super(event, SoundSource.MUSIC, SoundInstance.createUnseededRandom());
        this.layer = layer;
        this.looping = false;
        this.delay = 0;
        this.relative = true;
        this.attenuation = SoundInstance.Attenuation.NONE;
        this.pitch = 1.0F;
        this.volume = Math.max(layer.gain, 0.0001F);
    }

    @Override
    public void tick() {
        this.volume = Math.max(layer.gain, 0.0001F);
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }
}
