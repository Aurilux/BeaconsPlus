package aurilux.beaconsplus.common;

import aurilux.beaconsplus.common.block.LushBlock;
import aurilux.beaconsplus.common.block.ShroudBlock;
import aurilux.beaconsplus.common.container.LushContainer;
import aurilux.beaconsplus.common.container.ShroudContainer;
import aurilux.beaconsplus.common.potion.PotionBinding;
import aurilux.beaconsplus.common.potion.PotionDampen;
import aurilux.beaconsplus.common.potion.PotionFrailty;
import aurilux.beaconsplus.common.tile.LushTile;
import aurilux.beaconsplus.common.tile.ShroudTile;
import net.minecraft.block.Block;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.potion.Effect;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;

public class ModObjects {
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, BeaconsPlusMod.ID);
    private static final DeferredRegister<TileEntityType<?>> TILE_ENTITIES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, BeaconsPlusMod.ID);
    private static final DeferredRegister<Effect> POTIONS = DeferredRegister.create(ForgeRegistries.POTIONS, BeaconsPlusMod.ID);
    private static final DeferredRegister<ContainerType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS, BeaconsPlusMod.ID);

    // BLOCKS
    public static final RegistryObject<ShroudBlock> SHROUD_BLOCK = BLOCKS.register("shroud", ShroudBlock::new);
    public static final RegistryObject<LushBlock> LUSH_BLOCK = BLOCKS.register("lush", LushBlock::new);

    // TILE ENTITIES
    public static final RegistryObject<TileEntityType<ShroudTile>> SHROUD_TILE = TILE_ENTITIES.register("shroud",
            () -> TileEntityType.Builder.create(ShroudTile::new, SHROUD_BLOCK.get()).build(null));
    public static final RegistryObject<TileEntityType<LushTile>> LUSH_TILE = TILE_ENTITIES.register("lush",
            () -> TileEntityType.Builder.create(LushTile::new, LUSH_BLOCK.get()).build(null));

    // POTIONS
    public static final RegistryObject<PotionBinding> BINDING = POTIONS.register("binding", PotionBinding::new);
    public static final RegistryObject<PotionFrailty> FRAILTY = POTIONS.register("frailty", PotionFrailty::new);
    public static final RegistryObject<PotionDampen> DAMPEN = POTIONS.register("dampen", PotionDampen::new);

    //CONTAINERS
    public static final RegistryObject<ContainerType<ShroudContainer>> SHROUD_CONTAINER = CONTAINERS.register("shroud_container", () -> new ContainerType<>(ShroudContainer::new));
    public static final RegistryObject<ContainerType<LushContainer>> LUSH_CONTAINER = CONTAINERS.register("lush_container", () -> new ContainerType<>(LushContainer::new));


    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        TILE_ENTITIES.register(eventBus);
        POTIONS.register(eventBus);
        CONTAINERS.register(eventBus);
        eventBus.addGenericListener(Item.class, ModObjects::onRegisterItems);
    }

    public static void onRegisterItems(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> registry = event.getRegistry();
        BLOCKS.getEntries().stream()
                .map(RegistryObject::get)
                .forEach(block -> {
                    Item.Properties properties = new Item.Properties().group(BeaconsPlusMod.itemGroup);
                    BlockItem blockItem = new BlockItem(block, properties);
                    blockItem.setRegistryName(block.getRegistryName());
                    registry.register(blockItem);
                });
    }
}