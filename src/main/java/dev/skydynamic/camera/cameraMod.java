package dev.skydynamic.camera;

import net.fabricmc.api.ModInitializer;
//#if MC>=11904
//$$ import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
//#else
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
//#endif
import dev.skydynamic.camera.commands.cameraCommand;

public class cameraMod implements ModInitializer {

    @Override
    public void onInitialize() {

        //#if MC>=11904
        //$$ CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> new cameraCommand().registerCameraCommand(dispatcher));
        //#else
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> new cameraCommand().registerCameraCommand(dispatcher));
        //#endifs

    }

}
