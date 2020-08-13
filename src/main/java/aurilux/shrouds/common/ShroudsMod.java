package aurilux.shrouds.common;

import aurilux.shrouds.client.ShroudScreen;
import aurilux.shrouds.client.ShroudTileEntityRenderer;
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

@Mod(ShroudsMod.ID)
public class ShroudsMod {
    public static final String ID = "shrouds";
    public static final Logger LOG = LogManager.getLogger(ID.toUpperCase());

    public static ItemGroup itemGroup = new ItemGroup(ID) {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(ModObjects.SHROUD_BLOCK.get());
        }
    };

    public ShroudsMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::commonSetup);
        modBus.addListener(this::clientSetup);

        ModObjects.register(modBus);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        PacketHandler.init();
    }

    private void clientSetup(FMLClientSetupEvent event) {
        RenderTypeLookup.setRenderLayer(ModObjects.SHROUD_BLOCK.get(), RenderType.getCutout());
        ClientRegistry.bindTileEntityRenderer(ModObjects.SHROUD_TILE.get(), ShroudTileEntityRenderer::new);
        ScreenManager.registerFactory(ModObjects.SHROUD_CONTAINER.get(), ShroudScreen::new);
    }
}