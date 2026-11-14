package com.frierenmusic;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(value = "frierenmusic", dist = Dist.CLIENT)
public class FrierenMusicClient {
    public static final Logger LOGGER = LoggerFactory.getLogger("frierenmusic");

    public FrierenMusicClient() {
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, e -> MusicController.tick(Minecraft.getInstance()));
        NeoForge.EVENT_BUS.addListener(ClientPlayerNetworkEvent.LoggingOut.class, e -> MusicController.reset(Minecraft.getInstance()));
    }
}
