package aurilux.shrouds.common;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class PacketHandler {
    private static String protocol = "1";
    private static SimpleChannel CHANNEL;

    public static void init() {
        CHANNEL = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(ShroudsMod.ID, "main"))
                .networkProtocolVersion(() -> protocol)
                .clientAcceptedVersions(protocol::equals)
                .serverAcceptedVersions(protocol::equals)
                .simpleChannel();

        int id = 0;
        CHANNEL.registerMessage(id++, PacketUpdateShroud.class, PacketUpdateShroud::encode,
                PacketUpdateShroud::decode, PacketUpdateShroud.Handler::handle);
    }

    public static void sendToServer(Object msg) {
        CHANNEL.sendToServer(msg);
    }
}