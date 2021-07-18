package aurilux.beaconsplus.client.render;

import aurilux.beaconsplus.common.BeaconsPlusMod;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LushTileEntityRenderer extends BeaconVariantTileEntityRenderer {
    public LushTileEntityRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn, new ResourceLocation(BeaconsPlusMod.ID, "textures/entity/lush_beam.png"));
    }
}