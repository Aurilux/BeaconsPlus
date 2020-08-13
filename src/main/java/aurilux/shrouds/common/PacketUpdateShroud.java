package aurilux.shrouds.common;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketUpdateShroud {
    private final int primaryEffect;
    private final int secondaryEffect;

    public PacketUpdateShroud(int primaryEffectIn, int secondaryEffectIn) {
        this.primaryEffect = primaryEffectIn;
        this.secondaryEffect = secondaryEffectIn;
    }

    public static void encode(PacketUpdateShroud message, PacketBuffer buf) {
        buf.writeVarInt(message.primaryEffect);
        buf.writeVarInt(message.secondaryEffect);
    }

    public static PacketUpdateShroud decode(PacketBuffer buf) {
        return new PacketUpdateShroud(buf.readVarInt(), buf.readVarInt());
    }

    public static class Handler {
        public static void handle(final PacketUpdateShroud packet, final Supplier<NetworkEvent.Context> context) {
            context.get().enqueueWork(() -> {
                PlayerEntity player = context.get().getSender();
                if (player != null) {
                    if (player.openContainer instanceof ShroudContainer) {
                        ((ShroudContainer) player.openContainer).handleSlots(packet.primaryEffect, packet.secondaryEffect);
                    }
                }
            });
            context.get().setPacketHandled(true);
        }
    }
}