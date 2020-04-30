package mod.linguardium.pocketplants.blocks;

import mod.linguardium.pocketplants.api.PlantTag;
import mod.linguardium.pocketplants.utils.Nbt;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.util.Tickable;
import net.minecraft.world.GameRules;

public class PlantTerrariumEntity extends BlockEntity implements BlockEntityClientSerializable, Tickable {
    CompoundTag plantTag = null;
    float multiplier;
    public PlantTerrariumEntity() {
        super(initBlocks.TERRARIUM_ENTITY_TYPE);
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
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        if (plantTag != null && PlantTag.validate(plantTag))
            tag.put("PlantTag",plantTag);
        tag.putFloat("multiplier",multiplier);
        return super.toTag(tag);
    }

    @Override
    public void fromClientTag(CompoundTag tag) {
        this.fromTag(tag);
    }

    @Override
    public CompoundTag toClientTag(CompoundTag tag) {
        return this.toTag(tag);
    }

    @Override
    public void tick() {
        if (!world.isClient()) {
            boolean randomTick = world.random.nextInt(4096) < (int)(world.getGameRules().getInt(GameRules.RANDOM_TICK_SPEED) * multiplier);
            if (randomTick) {
                if (plantTag != null) {
                    PlantTag pTag = PlantTag.fromTag(plantTag);
                    if (pTag != null && !pTag.isMature()) {
                        pTag.grow();
                        setPlantTag(pTag);
                    }
                }
            }
        }
    }
}
