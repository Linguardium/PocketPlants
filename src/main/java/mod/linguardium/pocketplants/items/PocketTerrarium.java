package mod.linguardium.pocketplants.items;

import mod.linguardium.pocketplants.PocketPlants;
import mod.linguardium.pocketplants.api.PlantTag;
import mod.linguardium.pocketplants.utils.Nbt;
import net.minecraft.block.*;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

import java.util.List;

import static mod.linguardium.pocketplants.items.initItems.*;


public class PocketTerrarium extends BlockItem {

    public PocketTerrarium(Block block, Settings settings) {
        super(block, settings);
    }

    protected BlockPos canGrab(ItemStack terrariumStack, World world, BlockPos pos) {
         if (world != null && pos != null) {
             BlockState state = world.getBlockState(pos);
             CompoundTag tag = NbtHelper.fromBlockState(state);
             Block block = state.getBlock();
             Block base = world.getBlockState(pos.offset(Direction.DOWN)).getBlock();
             if (PocketPlants.getConfig().isBlacklisted(Registry.BLOCK.getId(block).getNamespace())) {
                 return null;
             }
             if (block instanceof CropBlock && !(base instanceof CropBlock)) {
                 return pos;
             }
             if (block instanceof FlowerBlock && !(base instanceof FlowerBlock)) {
                 return pos;
             }
             if (block instanceof TallFlowerBlock || block instanceof SugarCaneBlock) {
                 while (base == block) {
                     pos = pos.offset(Direction.DOWN);
                     base = world.getBlockState(pos.offset(Direction.DOWN)).getBlock();
                     if (pos.getY() <= 0) {
                         return null;
                     }
                 }

                 return pos;
             }

             if (block instanceof KelpPlantBlock || block instanceof KelpBlock) {
                 while (base instanceof KelpPlantBlock || base instanceof KelpBlock) {
                     pos = pos.offset(Direction.DOWN);
                     base = world.getBlockState(pos.offset(Direction.DOWN)).getBlock();
                     if (pos.getY() <= 0) {
                         return null;
                     }
                 }
                 return pos;
             }
         }
        return null;
    }
    protected ItemStack AttemptGrabBlock(ItemStack thisStack, ItemUsageContext context) {
        ItemStack newStack = ItemStack.EMPTY.copy();
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockPos takePos = canGrab(thisStack,world,pos);
        if (takePos != null) {
            BlockState bState = world.getBlockState(takePos);
            Block block = bState.getBlock();
            CompoundTag tag = new CompoundTag().copyFrom(thisStack.getOrCreateTag());
            if (block instanceof FluidFillable) {
                newStack = new ItemStack(WATER_TERRARIUM);
                if (block instanceof KelpPlantBlock) {
                    bState = Blocks.KELP.getDefaultState();
                }
            }else if (block instanceof TallFlowerBlock || block instanceof FlowerBlock) {
                newStack = new ItemStack(FLOWER_TERRARIUM);
            }else{
                newStack = new ItemStack(CROP_TERRARIUM);
            }
            newStack.setTag(tag);
            PlantTag pTag = new PlantTag(world,takePos,bState);
            newStack.putSubTag("PlantTag",pTag);
            world.breakBlock(takePos,false);
            world.breakBlock(takePos.offset(Direction.DOWN),false);
            return newStack;
        }
        return newStack;
    }
    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (context.getWorld().isClient()) {
            return ActionResult.SUCCESS;
        }

        if (PocketPlants.getConfig().bAllowRefills || !context.getStack().getOrCreateTag().contains("PlantTag") ) {
            ItemStack stack = context.getStack();
            World world = context.getWorld();
            ItemStack grabbedStack = AttemptGrabBlock(stack, context);
            if (!grabbedStack.isEmpty()) {
                if (!context.getPlayer().isCreative()) {
                    stack.decrement(1);
                }
                if (!context.getPlayer().giveItemStack(grabbedStack)) {
                    context.getPlayer().dropStack(grabbedStack);
                }
                return ActionResult.SUCCESS;
            }
        }
        return super.useOnBlock(context);
    }

    @Override
    public ActionResult place(ItemPlacementContext context) {

        return super.place(context);
    }

    private boolean hasPlant(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        return (tag.contains("PlantTag"));
        }


    protected void addPlant(ItemStack stack, String block) {
        CompoundTag plantTag = new CompoundTag();
        plantTag.putInt("age",0);
        plantTag.putString("block",block);
        stack.putSubTag("Plant",plantTag);
    }

    public float getSpeedMultiplier(ItemStack terrariumStack) {
        if (PocketPlants.getConfig().bEnableSpeedIncrease) {
            float multi = terrariumStack.getOrCreateTag().getFloat("multiplier");
            if (multi < 1.0F) {
                setSpeedMultiplier(terrariumStack, 1.0F);
                multi = 1.0F;
            }
            return multi;
        }else{
            return 1.0F;
        }
    }
    protected void setSpeedMultiplier(ItemStack stack, float multi) {
        if (PocketPlants.getConfig().bEnableSpeedIncrease) {
            if (multi > 5000) {
                multi = 5000;
            }
            CompoundTag tag = stack.getOrCreateTag();
            tag.putFloat("multiplier", multi);
            stack.setTag(tag);
        }
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (!world.isClient()) {
            CompoundTag tag = stack.getOrCreateTag();
            if (tag.contains("PlantTag", Nbt.TagTypes.get(CompoundTag.class))) {
                PlantTag pTag = PlantTag.fromTag(tag.getCompound("PlantTag"));
                if (pTag.isMature()) {
                    String namespace = pTag.getBlockId().getNamespace();
                    if (namespace==null) { namespace = ""; }
                    if (!namespace.isEmpty() && PocketPlants.getConfig().isBlacklisted( namespace )) {
                        return TypedActionResult.pass(stack);
                    }
                    if (PocketPlants.getConfig().bEnableSpeedIncrease) {
                        setSpeedMultiplier(stack, getSpeedMultiplier(stack) + PocketPlants.getConfig().RateOfSpeedIncrease());
                    }
                    List<ItemStack> products = pTag.getPlantProductStack((ServerWorld)world,player.getBlockPos());
                    //find seeds to keep this from ballooning seed stock:
                    for (ItemStack product: products) {
                        if (!player.giveItemStack(product)) {
                            ItemScatterer.spawn(world, player.getX(), player.getY(), player.getZ(), product);
                        }
                    }
                    pTag.resetBlockStateAge();
                    tag.put("PlantTag",pTag.toTag());
                    stack.setTag(tag);
                    return TypedActionResult.pass(stack);
                }
            }
        }
        return TypedActionResult.pass(stack);
    }


    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        CompoundTag tag = stack.getOrCreateTag();
        if (!world.isClient()) {
            boolean randomTick = world.random.nextInt(4096) < (int)(world.getGameRules().getInt(GameRules.RANDOM_TICK_SPEED)*getSpeedMultiplier(stack));
            if (randomTick) {
                if (tag.contains("Plant")) {
                    CompoundTag oldTag = tag.getCompound("Plant");
                    tag.remove("Plant");
                    tag.put("PlantTag",PlantTag.fromOldTag(world,oldTag));
                }
                if (tag.contains("PlantTag")) {
                    PlantTag pTag = PlantTag.fromTag(tag.getCompound("PlantTag"));
                    if (!pTag.isMature()) {
                        pTag.grow();
                        tag.put("PlantTag",pTag);
                        stack.setTag(tag);
                    }
                }
            }
        }
        int status=0;
        if (tag.contains("PlantTag")) {
            PlantTag pTag = PlantTag.fromTag(tag.getCompound("PlantTag"));
            if (pTag != null) {
                if (pTag.isMature()) {
                    status = 3;
                } else {
                    status = ((float) pTag.getAge() / (float) pTag.getMaxAge() > 0.5F) ? 2 : 1;
                }
            }
        }
        stack.getOrCreateTag().putInt("CustomModelData",status);
        super.inventoryTick(stack, world, entity, slot, selected);
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        CompoundTag tag = stack.getOrCreateTag();
        if (tag.contains("PlantTag")) {
            PlantTag pTag = PlantTag.fromTag(tag.getCompound("PlantTag"));
            tooltip.add(new TranslatableText("info.pocketplants.age", pTag.getAge(), pTag.getMaxAge()));
            tooltip.addAll(pTag.getTooltips());
        }else{
            tooltip.add(new TranslatableText("info.pocketplants.empty1").formatted(Formatting.ITALIC).formatted(Formatting.GRAY));
            tooltip.add(new TranslatableText("info.pocketplants.empty2").formatted(Formatting.ITALIC).formatted(Formatting.GRAY));
        }
        super.appendTooltip(stack, world, tooltip, context);
    }

    @Override
    public Text getName(ItemStack stack) {
        Text name = new TranslatableText("item.pocketplants.terrarium");
        CompoundTag tag = stack.getOrCreateTag();
        if (tag.contains("PlantTag")) {
            PlantTag pTag = PlantTag.fromTag(tag.getCompound("PlantTag"));
            if (pTag != null) {
                name = new TranslatableText("info.pocketplants.nameconcat", new TranslatableText(pTag.getBlock().getTranslationKey()), name).formatted(Formatting.GREEN);
            }
        }
        return name;
    }
}
