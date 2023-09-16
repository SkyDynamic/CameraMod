package dev.skydynamic.camera.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.RemoveEntityStatusEffectS2CPacket;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;

import java.util.concurrent.ConcurrentHashMap;

import static net.minecraft.server.command.CommandManager.literal;


public class CameraCommand {

    private static final ConcurrentHashMap<String, playerEntityCameraData> playerCameraPosDataHashMap = new ConcurrentHashMap<>();

    public static void clearCache(){
        playerCameraPosDataHashMap.clear();
    }


    public void registerCameraCommand(CommandDispatcher<ServerCommandSource> dispatcher)
    {
        dispatcher.register(
            literal("c").executes(
                (c) -> cameraModeExecute(c.getSource())
            )
        );
        dispatcher.register(
            literal("s").executes(
                (s) -> survivalModeExecute(s.getSource())
            )
        );
        dispatcher.register(
            literal("spec").executes(
                (c) -> cameraModeExecute(c.getSource())
            )
        );
        dispatcher.register(
            literal("survival").executes(
                (s) -> survivalModeExecute(s.getSource())
            )
        );
    }

    public static class playerEntityCameraData
    {
        ServerWorld World;
        Vec3d Pos;
        Float Pitch;
        Float Yaw;

        public playerEntityCameraData(ServerWorld World, Vec3d Pos, Float Pitch, Float Yaw)
        {
            this.World = World;
            this.Pos = Pos;
            this.Pitch = Pitch;
            this.Yaw = Yaw;
        }

    }

    public int cameraModeExecute(ServerCommandSource source)
    {
        try {
            ServerPlayerEntity player = source.getPlayer();
            playerCameraPosDataHashMap.put(player.getName().getString(), getPlayerData(player));
            changePlayerGameMode(player, GameMode.SPECTATOR);
            player.setVelocity(0,0.1,0);
            player.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(player));
            playerEffect(false, player);
            return 1;
        }
        catch (CommandSyntaxException ignored) {
        return 0;
        }
    }

    public int survivalModeExecute(ServerCommandSource source)
    {
        try {
            ServerPlayerEntity player = source.getPlayer();

            if (player.interactionManager.getGameMode() == GameMode.SURVIVAL) {
                return 0;
            }

            if (!playerCameraPosDataHashMap.containsKey(player.getName().getString())
                && player.interactionManager.getGameMode() != GameMode.SURVIVAL) {
                changePlayerGameMode(player, GameMode.SURVIVAL);
                playerEffect(true, player);
            } else {
                playerEntityCameraData originData = playerCameraPosDataHashMap.get(player.getName().getString());
                Vec3d Pos = originData.Pos;
                player.teleport(originData.World, Pos.x, Pos.y, Pos.z, originData.Yaw, originData.Pitch);
                changePlayerGameMode(player, GameMode.SURVIVAL);
                playerEffect(true, player);
                playerCameraPosDataHashMap.remove(player.getName().getString());
            }
            return 1;
        }
        catch (CommandSyntaxException ignored) {
            return 0;
        }
    }

    public void changePlayerGameMode(ServerPlayerEntity player, GameMode gameMode) {

        //#if MC>=11701
        player.changeGameMode(gameMode);
        //#else if MC < 11701
        //$$ player.setGameMode(gameMode);
        //#endif

    }

    public playerEntityCameraData getPlayerData(ServerPlayerEntity player) {

        Vec3d Pos = player.getPos();
        //#if MC>=11701
        Float Pitch = player.getPitch();
        Float Yaw = player.getYaw();
        //#else if MC<11701
        //$$ Float Pitch = player.pitch;
        //$$ Float Yaw = player.yaw;
        //#endif

        //#if MC>=12000
        //$$ ServerWorld World = player.getServerWorld();
        //#else
        ServerWorld World = player.getWorld();
        //#endif

        return new playerEntityCameraData(World, Pos, Pitch, Yaw);

    }

    public void playerEffect(boolean remove, ServerPlayerEntity player) {

        //#if MC>=11701
        int EntityID = player.getId();
        //#else if MC < 11701
        //$$ int EntityID = player.getEntityId();
        //#endif
        if (remove) {
            player.networkHandler.sendPacket(new RemoveEntityStatusEffectS2CPacket(EntityID, StatusEffects.NIGHT_VISION));
            player.removeStatusEffect(StatusEffects.NIGHT_VISION);
            player.networkHandler.sendPacket(new RemoveEntityStatusEffectS2CPacket(EntityID, StatusEffects.CONDUIT_POWER));
            player.removeStatusEffect(StatusEffects.CONDUIT_POWER);
        } else {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 999999, 0, false, false));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.CONDUIT_POWER, 999999, 0, false, false));
        }
    }

}
