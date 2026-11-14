package com.frierenmusic;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/** Track lists are discovered from assets/frierenmusic/sounds.json by name prefix (calm_, combat_, boss_). */
public final class Tracks {
    private Tracks() {}

    public static final String NAMESPACE = "frierenmusic";
    public static final String DRAGON_THEME = "boss_divine_revolte";

    private static List<String> calm, combat, boss;

    public static List<String> calm() { load(); return calm; }
    public static List<String> combat() { load(); return combat; }
    public static List<String> boss() { load(); return boss; }

    private static void load() {
        if (calm != null) return;
        calm = new ArrayList<>();
        combat = new ArrayList<>();
        boss = new ArrayList<>();
        try {
            var res = Minecraft.getInstance().getResourceManager()
                    .getResource(ResourceLocation.fromNamespaceAndPath(NAMESPACE, "sounds.json"));
            if (res.isPresent()) {
                try (Reader r = res.get().openAsReader()) {
                    JsonElement root = JsonParser.parseReader(r);
                    for (String key : root.getAsJsonObject().keySet()) {
                        if (key.startsWith("calm_")) calm.add(key);
                        else if (key.startsWith("combat_")) combat.add(key);
                        else if (key.startsWith("boss_")) boss.add(key);
                    }
                }
            }
        } catch (Exception e) {
            FrierenMusicClient.LOGGER.error("Failed to read sounds.json", e);
        }
        FrierenMusicClient.LOGGER.info("Tracks: {} calm, {} combat, {} boss", calm.size(), combat.size(), boss.size());
    }
}
