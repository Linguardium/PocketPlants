package mod.linguardium.pocketplants.items;

import mod.linguardium.pocketplants.PocketPlants;
import net.fabricmc.fabric.api.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class initItems {

    public static final PocketTerrarium TERRARIUM = new PocketTerrarium(new Item.Settings().group(ItemGroup.MISC).maxCount(64));
    public static final PocketTerrarium WATER_TERRARIUM = new PocketTerrarium(new Item.Settings().group(ItemGroup.MISC).maxCount(1));
    public static final PocketTerrarium CROP_TERRARIUM = new PocketTerrarium(new Item.Settings().group(ItemGroup.MISC).maxCount(1));
    public static final PocketTerrarium FLOWER_TERRARIUM = new PocketTerrarium(new Item.Settings().group(ItemGroup.MISC).maxCount(1));
    public static final Block TESTBLOCK = new Block(FabricBlockSettings.of(Material.WOOD).build());

    public static void initItems() {
        Registry.register(Registry.ITEM,new Identifier(PocketPlants.MOD_ID,"terrarium"),TERRARIUM);
        Registry.register(Registry.ITEM,new Identifier(PocketPlants.MOD_ID,"water_terrarium"),WATER_TERRARIUM);
        Registry.register(Registry.ITEM,new Identifier(PocketPlants.MOD_ID,"flower_terrarium"),FLOWER_TERRARIUM);
        Registry.register(Registry.ITEM,new Identifier(PocketPlants.MOD_ID,"crop_terrarium"),CROP_TERRARIUM);
        Registry.register(Registry.ITEM,new Identifier(PocketPlants.MOD_ID,"test_block"),new BlockItem(TESTBLOCK,new Item.Settings().group(ItemGroup.MISC)));

    }

}
