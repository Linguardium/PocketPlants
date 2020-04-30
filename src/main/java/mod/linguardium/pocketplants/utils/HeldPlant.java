package mod.linguardium.pocketplants.utils;

//import li.cryx.convth.block.AbstractResourcePlant;
import mod.linguardium.pocketplants.api.PlantTag;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.List;

class HeldPlant {
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
    public static BlockEntity getPlantBlockEntity(ItemStack terrariumStack) {
        CompoundTag tag = terrariumStack.getOrCreateSubTag("Plant");
        if (!tag.isEmpty()) {
            String blockID = tag.getString("blockEntity");
            if (!blockID.isEmpty()) {
                return Registry.BLOCK_ENTITY_TYPE.get(new Identifier(blockID)).instantiate();
            }
        }
        return null;
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
        List<ItemStack> newStack = DefaultedList.of();
        CompoundTag tag = terrariumStack.getOrCreateTag();

        if (!tag.contains("PlantTag")) {
            return newStack;
        }
        PlantTag pTag = PlantTag.fromTag(tag.getCompound("PlantTag"));
        Item seedItem = null;
        Block bPlant = pTag.getBlock();
/*        LootContext.Builder builder = new LootContext.Builder(world).setRandom(world.random)
                .put(LootContextParameters.BLOCK_STATE, pTag.getBlockState())
                .putNullable(LootContextParameters.BLOCK_ENTITY,pTag.getBlockEntity())
                .put(LootContextParameters.TOOL, ItemStack.EMPTY)
                .put(LootContextParameters.POSITION,player.getBlockPos());
  */    if (bPlant==null) {
            return newStack;
        }
        if (bPlant instanceof CropBlock) {
            seedItem = bPlant.asItem();
        }
        newStack = Block.getDroppedStacks(pTag.getBlockState(),world,player.getBlockPos(),pTag.getBlockEntity());

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
