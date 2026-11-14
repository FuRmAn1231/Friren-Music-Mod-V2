package com.frierenmusic.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.MusicManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Disables vanilla background music while in a world (the main menu keeps its vanilla music). */
@Mixin(MusicManager.class)
public class MusicManagerMixin {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void frierenmusic$disableVanillaMusic(CallbackInfo ci) {
        if (Minecraft.getInstance().level != null) {
            ci.cancel();
        }
    }
}
