package mod.linguardium.pocketplants.blocks;

import net.fabricmc.fabric.api.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import static mod.linguardium.pocketplants.PocketPlants.MOD_ID;

public class initBlocks {
    public static final Block WATER_SECTION = new Block(FabricBlockSettings.of(Material.WATER).breakInstantly().breakByHand(true).nonOpaque().noCollision().dropsNothing().build());
    public static final PlantTerrariumBlock TERRARIUM_BLOCK = new PlantTerrariumBlock(FabricBlockSettings.of(Material.WOOD).strength(2,2).breakByHand(true).nonOpaque().lightLevel(8).sounds(BlockSoundGroup.GLASS).build());
    public static final PlantTerrariumBlock WATER_TERRARIUM_BLOCK = new PlantTerrariumBlock(FabricBlockSettings.of(Material.WOOD).strength(2,2).breakByHand(true).nonOpaque().lightLevel(8).sounds(BlockSoundGroup.GLASS).build());
    public static BlockEntityType<PlantTerrariumEntity> TERRARIUM_ENTITY_TYPE;
    public static void init() {
        Registry.register(Registry.BLOCK,new Identifier(MOD_ID,"terrarium_block"),TERRARIUM_BLOCK);
        Registry.register(Registry.BLOCK,new Identifier(MOD_ID,"water_terrarium_block"),WATER_TERRARIUM_BLOCK);
        TERRARIUM_ENTITY_TYPE = Registry.register(Registry.BLOCK_ENTITY_TYPE,new Identifier(MOD_ID,"terrarium_entity"), BlockEntityType.Builder.create(PlantTerrariumEntity::new, TERRARIUM_BLOCK).build(null));
    }
}
