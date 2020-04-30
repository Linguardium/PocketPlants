package mod.linguardium.pocketplants.items;

import mod.linguardium.pocketplants.PocketPlants;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import static mod.linguardium.pocketplants.blocks.initBlocks.TERRARIUM_BLOCK;

public class initItems {

    public static final PocketTerrarium TERRARIUM = new PocketTerrarium(TERRARIUM_BLOCK, new Item.Settings().group(ItemGroup.MISC).maxCount(64));
    public static final PocketTerrarium WATER_TERRARIUM = new PocketTerrarium(TERRARIUM_BLOCK, new Item.Settings().group(ItemGroup.MISC).maxCount(1));
    public static final PocketTerrarium CROP_TERRARIUM = new PocketTerrarium(TERRARIUM_BLOCK, new Item.Settings().group(ItemGroup.MISC).maxCount(1));
    public static final PocketTerrarium FLOWER_TERRARIUM = new PocketTerrarium(TERRARIUM_BLOCK, new Item.Settings().group(ItemGroup.MISC).maxCount(1));

    public static void initItems() {
        Registry.register(Registry.ITEM,new Identifier(PocketPlants.MOD_ID,"terrarium"),TERRARIUM);
        Registry.register(Registry.ITEM,new Identifier(PocketPlants.MOD_ID,"water_terrarium"),WATER_TERRARIUM);
        Registry.register(Registry.ITEM,new Identifier(PocketPlants.MOD_ID,"flower_terrarium"),FLOWER_TERRARIUM);
        Registry.register(Registry.ITEM,new Identifier(PocketPlants.MOD_ID,"crop_terrarium"),CROP_TERRARIUM);
    }

}
