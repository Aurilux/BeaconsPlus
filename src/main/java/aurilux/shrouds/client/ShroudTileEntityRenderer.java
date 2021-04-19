package aurilux.shrouds.client;

import aurilux.shrouds.common.ShroudTitleEntity;
import aurilux.shrouds.common.ShroudsMod;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.BeaconTileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Arrays;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ShroudTileEntityRenderer extends TileEntityRenderer<ShroudTitleEntity> {
    public static final ResourceLocation TEXTURE_BEACON_BEAM = new ResourceLocation(ShroudsMod.ID, "textures/entity/shroud_beam.png");

    public ShroudTileEntityRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    public void render(ShroudTitleEntity tileEntityIn, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        long time = tileEntityIn.getWorld().getGameTime();
        List<ShroudTitleEntity.BeamSegment> list = tileEntityIn.getBeamSegments();
        int yOffset = 0;

        for(int i = 0; i < list.size(); i++) {
            ShroudTitleEntity.BeamSegment beamSegment = list.get(i);
            renderBeamSegment(matrixStackIn, bufferIn, partialTicks, time, yOffset, i == list.size() - 1 ? 1024 : beamSegment.getHeight(), beamSegment.getColors());
            yOffset += beamSegment.getHeight();
        }

    }

    private static void renderBeamSegment(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, float partialTicks, long totalWorldTime, int yOffset, int height, float[] colors) {
        float[] dullColors = Arrays.copyOf(colors, colors.length);
        dullColors[0] = dullColors[0] * .6f;
        dullColors[1] = dullColors[1] * .6f;
        dullColors[2] = dullColors[2] * .6f;
        //BeaconTileEntityRenderer.renderBeamSegment(matrixStackIn, bufferIn, TEXTURE_BEACON_BEAM, partialTicks, 1.0F, totalWorldTime, yOffset, height, dullColors, 0.2F, 0.25F);
        BeaconTileEntityRenderer.renderBeamSegment(matrixStackIn, bufferIn, TEXTURE_BEACON_BEAM, partialTicks, 1.0F, totalWorldTime, yOffset, height, colors, 0.2F, 0.25F);
    }

    public boolean isGlobalRenderer(ShroudTitleEntity te) {
        return true;
    }
}