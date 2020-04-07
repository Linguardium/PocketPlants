package mod.linguardium.pocketplants.utils;

//import li.cryx.convth.block.AbstractResourcePlant;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.List;

public class HeldPlant {

    public static boolean hasPlant(ItemStack terrariumStack) {
        CompoundTag tag = terrariumStack.getOrCreateSubTag("Plant");
        if (!tag.isEmpty()) {
            String blockID = tag.getString("block");
            return !blockID.isEmpty();
        }
        return false;
    }

    public static void growContainedPlant(ItemStack terrariumStack) {
        if (!isMature(terrariumStack)) {
            setContainedPlantAge(terrariumStack, getContainedPlantAge(terrariumStack) + 1);
        }
    }

    public static int getContainedPlantAge(ItemStack terrariumStack) {
        CompoundTag tag = terrariumStack.getOrCreateSubTag("Plant");
        if (tag.isEmpty()) {
            return 0;
        }
        return tag.getInt("age");
    }
    public static void setContainedPlantAge(ItemStack terrariumStack, int age) {
        CompoundTag tag = terrariumStack.getOrCreateSubTag("Plant");
        if (!tag.isEmpty()) {
            tag.putInt("age", age);
        }
        terrariumStack.putSubTag("Plant", tag);
    }

    public static int getContainedPlantMaxAge(ItemStack terrariumStack) {
        Block plant = getPlantBlock(terrariumStack);
        if (plant instanceof CropBlock) {
            return ((CropBlock) plant).getMaxAge();
        }else if (plant instanceof SugarCaneBlock) {
            return 15;
        }else if (plant instanceof KelpBlock) {
            return 25;
        }else if (isFlowerPlant(terrariumStack)) {
            return 5;
        }
        return 0;
    }
    public static boolean isWaterPlant(ItemStack stack) {
        return (getPlantBlock(stack) instanceof FluidFillable);
    }
    public static boolean isFlowerPlant(ItemStack stack) {
        return (getPlantBlock(stack) instanceof FlowerBlock || getPlantBlock(stack) instanceof TallFlowerBlock);
    }

    public static Block getPlantBlock(ItemStack terrariumStack) {
        CompoundTag tag = terrariumStack.getOrCreateSubTag("Plant");
        if (!tag.isEmpty()) {
            String blockID = tag.getString("block");
            if (!blockID.isEmpty()) {
                return Registry.BLOCK.get(Identifier.tryParse(blockID));
            }
        }
        return Blocks.AIR;
    }
    public static String getPlantBlockId(ItemStack terrariumStack) {
        CompoundTag tag = terrariumStack.getOrCreateSubTag("Plant");
        if (!tag.isEmpty()) {
            return tag.getString("block");
        }
        return "";
    }
    public static boolean isMature(ItemStack stack) {
        return (getContainedPlantAge(stack) >= getContainedPlantMaxAge(stack));
    }
    public static List<ItemStack> getPlantProductStack(ServerWorld world, PlayerEntity player, ItemStack terrariumStack) {
        Block bPlant = getPlantBlock(terrariumStack);
        BlockState bState = null;
        List<ItemStack> newStack = DefaultedList.of();
        IntProperty ageProperty=null;
        Item seedItem = null;
        if (bPlant instanceof CropBlock) {
            ageProperty=((CropBlock) bPlant).getAgeProperty();
            seedItem = bPlant.asItem();
            /*
            *    Commented the below, i am pretending these will implement standard getDroppedStacks methods....
             */
/*            //Hopefully temporary hacky compatibility
            if (PocketPlants.getConfig().bEnableConvenientThingsSupport && FabricLoader.getInstance().isModLoaded("convth") && bPlant instanceof AbstractResourcePlant) {
                newStack = (ConvenientThings.getResourcePlantDrops(world,(AbstractResourcePlant)bPlant));
            }else {
                bState = bPlant.getDefaultState().with(((CropBlock) bPlant).getAgeProperty(), getContainedPlantAge(terrariumStack));
            }*/
        }else if(bPlant instanceof SugarCaneBlock) {
            ageProperty=SugarCaneBlock.AGE;
        }else if (bPlant instanceof KelpBlock) {
            ageProperty=KelpBlock.AGE;
        }else if (bPlant instanceof FlowerBlock || bPlant instanceof TallFlowerBlock) {
            newStack.add(new ItemStack(bPlant.asItem(),1));
        }
        if (ageProperty != null && newStack.size() == 0) {
            bState = bPlant.getDefaultState().with(ageProperty, getContainedPlantAge(terrariumStack));
            newStack = Block.getDroppedStacks(bState, world, player.getBlockPos(), null);
        }
        if (seedItem != null) {
            for (ItemStack iStack : newStack) {
                if (iStack.getItem().equals(seedItem)) {
                    iStack.decrement(1);
                    break;
                }
            }
        }
        return newStack;
    }

}
