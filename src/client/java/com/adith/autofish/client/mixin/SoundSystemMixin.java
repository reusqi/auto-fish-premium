package com.adith.autofish.client.mixin;

import com.adith.autofish.AutoFishManager;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SoundEngine.class)
public class SoundSystemMixin {

    @Inject(method = "play(Lnet/minecraft/client/resources/sounds/SoundInstance;)Lnet/minecraft/client/sounds/SoundEngine$PlayResult;", at = @At("HEAD"))
    private void onSoundPlayed(SoundInstance sound, CallbackInfoReturnable<SoundEngine.PlayResult> cir) {
        if (sound != null && sound.getIdentifier() != null && sound.getIdentifier().toString().equals("minecraft:entity.fishing_bobber.splash")) {
            AutoFishManager.handleSplashSound(sound.getX(), sound.getY(), sound.getZ());
        }
    }
}
