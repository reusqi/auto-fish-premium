package com.adith.autofish.client.mixin;

import com.adith.autofish.AutoFishManager;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void autofish$tick(CallbackInfo ci) {
        AutoFishManager.onPlayerTick((LocalPlayer) (Object) this);
    }
}
