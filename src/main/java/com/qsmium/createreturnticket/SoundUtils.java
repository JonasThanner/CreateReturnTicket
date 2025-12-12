package com.qsmium.createreturnticket;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;

public class SoundUtils {

    private static SoundManager soundManager;

    public static void playGlobalSound(Holder<SoundEvent> soundEventHolder, float volume, float pitch)
    {
        playGlobalSound(soundEventHolder.value(), pitch, volume);
    }

    public static void playGlobalSound(SoundEvent soundEvent, float volume,float pitch) {

        if(soundManager == null)
        {
            soundManager = Minecraft.getInstance().getSoundManager();
        }

        // Create a SimpleSoundInstance for non-positional sounds
        soundManager.play(
                SimpleSoundInstance.forUI(
                        soundEvent,
                        volume,
                        pitch
                )
        );
    }
}
