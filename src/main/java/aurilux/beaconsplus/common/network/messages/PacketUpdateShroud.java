package aurilux.shrouds.common.network.messages;

import aurilux.shrouds.common.ShroudContainer;
import net.minecraft.client.Minecraft;
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

    public static void encode(PacketUpdateShroud msg, PacketBuffer buf) {
        buf.writeVarInt(msg.primaryEffect);
        buf.writeVarInt(msg.secondaryEffect);
    }

    public static PacketUpdateShroud decode(PacketBuffer buf) {
        return new PacketUpdateShroud(buf.readVarInt(), buf.readVarInt());
    }

    public static void handle(PacketUpdateShroud msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(new Runnable() {
            // Have to use anon class instead of lambda or else we'll get classloading issues
            @Override
            public void run() {
                PlayerEntity player = Minecraft.getInstance().player;
                if (player != null) {
                    if (player.openContainer instanceof ShroudContainer) {
                        ((ShroudContainer) player.openContainer).handleSlots(msg.primaryEffect, msg.secondaryEffect);
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}