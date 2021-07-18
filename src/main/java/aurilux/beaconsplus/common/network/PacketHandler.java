package aurilux.beaconsplus.common.network;

import aurilux.beaconsplus.common.BeaconsPlusMod;
import aurilux.beaconsplus.common.network.messages.PacketUpdateShroud;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.util.Optional;

public class PacketHandler {
    private final static String protocol = "1";
    private static SimpleChannel CHANNEL;
    private static int index = 0;

    public static void init() {
        CHANNEL = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(BeaconsPlusMod.ID, "main"))
                .networkProtocolVersion(() -> protocol)
                .clientAcceptedVersions(protocol::equals)
                .serverAcceptedVersions(protocol::equals)
                .simpleChannel();

        // To client

        // To server
        CHANNEL.registerMessage(index++, PacketUpdateShroud.class,
                PacketUpdateShroud::encode,
                PacketUpdateShroud::decode,
                PacketUpdateShroud::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));

        // To both
    }

    public static void sendToServer(Object msg) {
        CHANNEL.sendToServer(msg);
    }

    public static void sendTo(Object msg, ServerPlayerEntity player) {
        CHANNEL.sendTo(msg, player.connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT);
    }

    public static void sendToAll(Object msg) {
        sendToAllExcept(msg, null);
    }

    public static void sendToAllExcept(Object msg, ServerPlayerEntity ignoredPlayer) {
        for (ServerPlayerEntity player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
            if (ignoredPlayer == null || player != ignoredPlayer) {
                CHANNEL.sendTo(msg, player.connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT);
            }
            else {
                BeaconsPlusMod.LOG.debug("Ignored Player: " + ignoredPlayer.getDisplayName().getString());
            }
        }
    }
}