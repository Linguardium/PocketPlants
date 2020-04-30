package mod.linguardium.pocketplants.blocks;

import mod.linguardium.pocketplants.PocketPlants;
import mod.linguardium.pocketplants.api.PlantTag;
import mod.linguardium.pocketplants.items.PocketTerrarium;
import mod.linguardium.pocketplants.utils.Nbt;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import java.util.List;

import static mod.linguardium.pocketplants.items.initItems.*;

public class PlantTerrariumBlock extends Block implements BlockEntityProvider {
    public PlantTerrariumBlock(Settings settings) {
        super(settings);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.INVISIBLE;
    }

    @Override
    public List<ItemStack> getDroppedStacks(BlockState state, LootContext.Builder builder) {
        List<ItemStack> stacks=super.getDroppedStacks(state, builder);
        List<ItemStack> newStacks = DefaultedList.of();
        BlockEntity blockEntity = builder.getNullable(LootContextParameters.BLOCK_ENTITY);
        if (blockEntity != null) {
            stacks.forEach((stack) -> {
                if (stack.getItem() instanceof PocketTerrarium) {
                    if (blockEntity instanceof PlantTerrariumEntity) {
                        CompoundTag tag = stack.getOrCreateTag();
                        tag.putFloat("multiplier", ((PlantTerrariumEntity) blockEntity).getMultiplier());
                        if (((PlantTerrariumEntity) blockEntity).getPlantTag() != null) {
                            tag.put("PlantTag", ((PlantTerrariumEntity) blockEntity).getPlantTag());
                            Block b = ((PlantTerrariumEntity) blockEntity).getPlantTag().getBlock();
                            if (b instanceof FluidFillable) {
                                stack = new ItemStack(WATER_TERRARIUM);
                            } else if (b instanceof FlowerBlock || b instanceof TallFlowerBlock) {
                                stack = new ItemStack(FLOWER_TERRARIUM);
                            } else {
                                stack = new ItemStack(CROP_TERRARIUM);
                            }
                        }
                        stack.setTag(tag);
                        newStacks.add(stack);
                    }
                }
            });
        }
        return newStacks;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient()) {
            return ActionResult.SUCCESS;
        }
        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof PlantTerrariumEntity) {
            PlantTag pTag = ((PlantTerrariumEntity) be).getPlantTag();
            if (pTag != null) {
                if (pTag.isMature()) {
                    String namespace = pTag.getBlockId().getNamespace();
                    if (namespace==null) { namespace = ""; }
                    if (!namespace.isEmpty() && PocketPlants.getConfig().isBlacklisted( namespace )) {
                        return ActionResult.PASS;
                    }
                    if (PocketPlants.getConfig().bEnableSpeedIncrease) {
                        ((PlantTerrariumEntity) be).setMultiplier(((PlantTerrariumEntity) be).getMultiplier()+ PocketPlants.getConfig().RateOfSpeedIncrease());
                    }
                    List<ItemStack> products = pTag.getPlantProductStack((ServerWorld)world,player.getBlockPos());
                    //find seeds to keep this from ballooning seed stock:
                    for (ItemStack product: products) {

                            ItemScatterer.spawn(world, pos.getX(), pos.getY()+1, pos.getZ(), product);
                    }
                    pTag.resetBlockStateAge();
                    ((PlantTerrariumEntity) be).setPlantTag(pTag);
                    return ActionResult.SUCCESS;
                }else{
                    return ActionResult.FAIL;
                }
            }
        }
        return super.onUse(state, world, pos, player, hand, hit);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        BlockEntity be = world.getBlockEntity(pos);
        CompoundTag tag = itemStack.getOrCreateTag();
        PlantTag pTag = null;
        if (tag.contains("PlantTag")) {
            pTag = PlantTag.fromTag(tag.getCompound("PlantTag"));
        }
        if (be instanceof PlantTerrariumEntity) {
            if (pTag != null) {
                ((PlantTerrariumEntity) be).setPlantTag(pTag);
            }
            if (tag.contains("multiplier", Nbt.TagTypes.get(FloatTag.class))) {
                ((PlantTerrariumEntity) be).setMultiplier(tag.getFloat("multiplier"));
            }
        }

    }

    @Override
    public BlockEntity createBlockEntity(BlockView view) {
        return new PlantTerrariumEntity();
    }
}
