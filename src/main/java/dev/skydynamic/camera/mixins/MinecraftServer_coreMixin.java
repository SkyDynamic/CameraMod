package dev.skydynamic.camera.mixins;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.Mixin;

import dev.skydynamic.camera.commands.CameraCommand;

@Mixin(MinecraftServer.class)
public class MinecraftServer_coreMixin {
    @Inject(method = "loadWorld", at = @At("RETURN"))
    private void serverLoadedWorlds(CallbackInfo ci)
    {
        CameraCommand.clearCache();
    }

}
