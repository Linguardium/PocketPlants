package mod.linguardium.pocketplants.items;

import mod.linguardium.pocketplants.PocketPlants;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class initItems {
    public static final PocketTerrarium TERRARIUM = new PocketTerrarium(new Item.Settings().group(ItemGroup.MISC).maxCount(1));
    public static void initItems() {
        Registry.register(Registry.ITEM,new Identifier(PocketPlants.MOD_ID,"terrarium"),TERRARIUM);
    }
}
