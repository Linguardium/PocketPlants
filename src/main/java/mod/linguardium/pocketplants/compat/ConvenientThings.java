package mod.linguardium.pocketplants.compat;

import mod.linguardium.pocketplants.PocketPlants;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DefaultedList;
import li.cryx.convth.block.AbstractResourcePlant;
import net.minecraft.world.World;
import org.apache.logging.log4j.Level;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public class ConvenientThings {
    public static DefaultedList<ItemStack> getResourcePlantDrops(World world, AbstractResourcePlant plantBlock) {
        DefaultedList<ItemStack> items = DefaultedList.of();
        try {
            // Some reflection to get private stuff...
            Field SEEDDROPCHANCE = AbstractResourcePlant.class.getDeclaredField("SEED_DROP_CHANCE");
            SEEDDROPCHANCE.setAccessible(true);
            Method getResourceItem = AbstractResourcePlant.class.getDeclaredMethod("getResourceItem");
            getResourceItem.setAccessible(true);
            int SeedDropChance = SEEDDROPCHANCE.getInt(plantBlock);
        if (world.random.nextInt(100) < SeedDropChance) {
            items.add(new ItemStack(plantBlock.getSeedsItem(), 1));
        }

        Item resItem = (Item)getResourceItem.invoke(plantBlock);
            items.add(new ItemStack(resItem));

        } catch (NoSuchFieldException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            PocketPlants.log(Level.ERROR,"Could not reflect to get access to ConvenientThings plant");
        }
        return items;
    }
}
