package mod.linguardium.pocketplants.blocks;

import mod.linguardium.pocketplants.api.PlantTag;
import mod.linguardium.pocketplants.impl.TerrariumInventory;
import mod.linguardium.pocketplants.utils.Nbt;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.Tickable;
import net.minecraft.world.GameRules;

import java.util.List;

public class PlantTerrariumEntity extends BlockEntity implements BlockEntityClientSerializable, Tickable, TerrariumInventory {
    CompoundTag plantTag = null;
    float multiplier;
    public PlantTerrariumEntity() {
        super(initBlocks.TERRARIUM_ENTITY_TYPE);
    }
    DefaultedList<ItemStack> inventory = DefaultedList.ofSize(4,ItemStack.EMPTY);

    @Override
    public DefaultedList<ItemStack> getItems() {
        return inventory;
    }

    public PlantTag getPlantTag() {
        if (plantTag != null) {
            return PlantTag.fromTag(plantTag);
        }
        return null;
    }
    public void setPlantTag(PlantTag pTag) {
        if (pTag!=null && PlantTag.validate(pTag)) {
            plantTag = pTag;
            this.markDirty();
            sync();
        }
    }
    public void setMultiplier(float multi) {
        this.multiplier=multi;
    }
    public float getMultiplier() {
        return this.multiplier;
    }
    @Override
    public void fromTag(CompoundTag tag) {
        super.fromTag(tag);
        if (tag.contains("multiplier", Nbt.TagTypes.get(FloatTag.class))) {
            this.multiplier=tag.getFloat("multiplier");
        }
        if (tag.contains("PlantTag", Nbt.TagTypes.get(CompoundTag.class))) {
            plantTag = PlantTag.fromTag(tag.getCompound("PlantTag"));
        }
        Inventories.fromTag(tag,inventory);
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        if (plantTag != null && PlantTag.validate(plantTag))
            tag.put("PlantTag",plantTag);
        tag.putFloat("multiplier",multiplier);
        Inventories.toTag(tag,inventory,true);
        return super.toTag(tag);
    }

    @Override
    public void markDirty() {
        ifEmptyResetAge();
        super.markDirty();
        sync();
    }

    @Override
    public void fromClientTag(CompoundTag tag) {
        this.fromTag(tag);
    }

    @Override
    public CompoundTag toClientTag(CompoundTag tag) {
        return this.toTag(tag);
    }
    public void collectMatureCrop(PlantTag pTag) {
        if (pTag!=null) {
            List<ItemStack> stacks = pTag.getPlantProductStack((ServerWorld) world, pos);
            for (ItemStack stack : stacks) {
                insertIntoInventory(stack);
            }
        }
    }
    @Override
    public void tick() {
        if (!world.isClient()) {
            boolean randomTick = world.random.nextInt(4096) < (int)(world.getGameRules().getInt(GameRules.RANDOM_TICK_SPEED) * multiplier);
            if (randomTick && world.random.nextInt(9)==0) {
                if (plantTag != null) {
                    PlantTag pTag = PlantTag.fromTag(plantTag);
                    if (pTag != null && !pTag.isMature()) {
                        pTag.grow();
                        if (pTag.isMature()) {
                            collectMatureCrop(pTag);
                        }
                        setPlantTag(pTag);
                    }
                }
            }
        }
    }
}
