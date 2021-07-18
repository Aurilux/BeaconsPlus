package aurilux.beaconsplus.common;

import aurilux.beaconsplus.client.render.LushTileEntityRenderer;
import aurilux.beaconsplus.client.render.ShroudTileEntityRenderer;
import aurilux.beaconsplus.client.screen.LushScreen;
import aurilux.beaconsplus.client.screen.ShroudScreen;
import aurilux.beaconsplus.common.network.PacketHandler;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;

@Mod(BeaconsPlusMod.ID)
public class BeaconsPlusMod {
    public static final String ID = "beaconsplus";
    public static final Logger LOG = LogManager.getLogger(ID.toUpperCase());

    public static ItemGroup itemGroup = new ItemGroup(ID) {
        @Nonnull
        @Override
        public ItemStack createIcon() {
            return new ItemStack(ModObjects.SHROUD_BLOCK.get());
        }
    };

    public BeaconsPlusMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::commonSetup);
        modBus.addListener(this::clientSetup);

        ModObjects.register(modBus);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        PacketHandler.init();
    }

    private void clientSetup(FMLClientSetupEvent event) {
        RenderTypeLookup.setRenderLayer(ModObjects.SHROUD_BLOCK.get(), RenderType.getTranslucent());
        ClientRegistry.bindTileEntityRenderer(ModObjects.SHROUD_TILE.get(), ShroudTileEntityRenderer::new);
        ScreenManager.registerFactory(ModObjects.SHROUD_CONTAINER.get(), ShroudScreen::new);

        RenderTypeLookup.setRenderLayer(ModObjects.LUSH_BLOCK.get(), RenderType.getTranslucent());
        ClientRegistry.bindTileEntityRenderer(ModObjects.LUSH_TILE.get(), LushTileEntityRenderer::new);
        ScreenManager.registerFactory(ModObjects.LUSH_CONTAINER.get(), LushScreen::new);
    }
}