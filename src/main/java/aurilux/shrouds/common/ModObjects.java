package aurilux.shrouds.common;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
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
    private static final DeferredRegister<Block> BLOCKS = new DeferredRegister<>(ForgeRegistries.BLOCKS, ShroudsMod.ID);
    private static final DeferredRegister<TileEntityType<?>> TILE_ENTITIES = new DeferredRegister<>(ForgeRegistries.TILE_ENTITIES, ShroudsMod.ID);
    private static final DeferredRegister<Effect> POTIONS = new DeferredRegister<>(ForgeRegistries.POTIONS, ShroudsMod.ID);
    private static final DeferredRegister<ContainerType<?>> CONTAINERS = new DeferredRegister<>(ForgeRegistries.CONTAINERS, ShroudsMod.ID);

    // BLOCKS
    public static final RegistryObject<ShroudBlock> SHROUD_BLOCK = BLOCKS.register("shroud", () -> new ShroudBlock(Block.Properties.create(Material.GLASS, MaterialColor.DIAMOND).hardnessAndResistance(3.0F).lightValue(15).notSolid()));

    // TILE ENTITIES
    public static final RegistryObject<TileEntityType<ShroudTitleEntity>> SHROUD_TILE = TILE_ENTITIES.register("shroud", () ->
            TileEntityType.Builder.create(ShroudTitleEntity::new, SHROUD_BLOCK.get())
                    .build(null));

    // POTIONS
    public static final RegistryObject<PotionWeight> WEIGHT = POTIONS.register("weight", PotionWeight::new);

    //CONTAINERS
    public static final RegistryObject<ContainerType<ShroudContainer>> SHROUD_CONTAINER = CONTAINERS.register("shroud_container", () -> new ContainerType<>(ShroudContainer::new));

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        TILE_ENTITIES.register(eventBus);
        POTIONS.register(eventBus);
        CONTAINERS.register(eventBus);
        eventBus.addListener(ModObjects::onRegisterItems);
    }

    public static void onRegisterItems(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> registry = event.getRegistry();
        // Not even sure why this check needs to be here. Without it the game crashes with an error of "BlockItem cannot
        // be cast to Block". The debug log also prints "Firing block registry for mod shrouds" before it crashes. It
        // sends every RegistryEvent, not just ITEMS. Somehow.
        // It seems this only happens when registering a static reference (TestClass::testMethod). It does not through
        // instance reference (TestClass.testMethod), or through the EventSubscriber annotation
        if (registry.equals(ForgeRegistries.ITEMS)) {
            BLOCKS.getEntries().stream()
                    .map(RegistryObject::get)
                    .forEach(block -> {
                        Item.Properties properties = new Item.Properties().group(ShroudsMod.itemGroup);
                        BlockItem blockItem = new BlockItem(block, properties);
                        blockItem.setRegistryName(block.getRegistryName());
                        registry.register(blockItem);
                    });
        }
    }
}