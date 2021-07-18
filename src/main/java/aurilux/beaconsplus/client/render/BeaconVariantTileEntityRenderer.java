package aurilux.beaconsplus.client.render;

import aurilux.beaconsplus.common.tile.BeaconVariantTile;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.BeaconTileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class BeaconVariantTileEntityRenderer extends TileEntityRenderer<BeaconVariantTile> {
    private final ResourceLocation beamTexture;

    public BeaconVariantTileEntityRenderer(TileEntityRendererDispatcher rendererDispatcherIn, ResourceLocation texture) {
        super(rendererDispatcherIn);
        beamTexture = texture;
    }

    @Override
    public void render(BeaconVariantTile tileEntityIn, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        long time = tileEntityIn.getWorld().getGameTime();
        List<BeaconVariantTile.BeamSegment> list = tileEntityIn.getBeamSegments();
        int yOffset = 0;

        for(int i = 0; i < list.size(); i++) {
            BeaconVariantTile.BeamSegment beamSegment = list.get(i);
            renderBeamSegment(matrixStackIn, bufferIn, partialTicks, time, yOffset, i == list.size() - 1 ? 1024 : beamSegment.getHeight(), beamSegment.getColors());
            yOffset += beamSegment.getHeight();
        }

    }

    private void renderBeamSegment(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, float partialTicks, long totalWorldTime, int yOffset, int height, float[] colors) {
        BeaconTileEntityRenderer.renderBeamSegment(matrixStackIn, bufferIn, beamTexture, partialTicks, 1.0F, totalWorldTime, yOffset, height, colors, 0.2F, 0.25F);
    }

    public boolean isGlobalRenderer(BeaconVariantTile te) {
        return true;
    }
}
