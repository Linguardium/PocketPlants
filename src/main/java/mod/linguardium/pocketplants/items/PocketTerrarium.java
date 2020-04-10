package mod.linguardium.pocketplants.items;

import mod.linguardium.pocketplants.PocketPlants;
import net.minecraft.block.*;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

import static mod.linguardium.pocketplants.items.initItems.*;
import static mod.linguardium.pocketplants.utils.HeldPlant.*;

import java.util.List;


public class PocketTerrarium extends Item {

    public PocketTerrarium(Settings settings) {
        super(settings);
    }

    protected BlockPos canGrab(ItemStack terrariumStack, World world, BlockPos pos) {
         if (world != null && pos != null) {
            Block block = world.getBlockState(pos).getBlock();
            Block base = world.getBlockState(pos.offset(Direction.DOWN)).getBlock();
            if (PocketPlants.getConfig().isBlacklisted( Registry.BLOCK.getId(block).getNamespace())) {
                return null;
            }
            if (block instanceof CropBlock && !(base instanceof CropBlock)) {
                return pos;
            }
             if (block instanceof FlowerBlock && !(base instanceof FlowerBlock)) {
                 return pos;
             }
            if (block instanceof TallFlowerBlock) {
                while (base instanceof TallFlowerBlock) {
                    pos = pos.offset(Direction.DOWN);
                    base = world.getBlockState(pos.offset(Direction.DOWN)).getBlock();
                    if (pos.getY() <= 0) {
                        return null;
                    }
                }
                return pos;
            }
            if (block instanceof SugarCaneBlock) {
                while (base instanceof SugarCaneBlock) {
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
    protected ItemStack AttemptGrabBlock(ItemStack thisStack, World world, BlockPos pos) {
        ItemStack newStack = ItemStack.EMPTY.copy();
        BlockPos takePos = canGrab(thisStack,world,pos);
        if (takePos != null) {
            BlockState bState = world.getBlockState(takePos);
            Block block = bState.getBlock();

            CompoundTag tag = new CompoundTag().copyFrom(thisStack.getOrCreateTag());
            if (block instanceof FluidFillable) {
                newStack = new ItemStack(WATER_TERRARIUM);
                if (block instanceof KelpPlantBlock) {
                    block = Blocks.KELP;
                }
            }else if (block instanceof TallFlowerBlock || block instanceof FlowerBlock) {
                newStack = new ItemStack(FLOWER_TERRARIUM);
            }else{
                newStack = new ItemStack(CROP_TERRARIUM);
            }
            newStack.setTag(tag);
            addPlant(newStack, Registry.BLOCK.getId(block).toString());
            world.breakBlock(takePos,false);
            world.breakBlock(takePos.offset(Direction.DOWN),false);
            return newStack;
        }
        return newStack;
    }
    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (PocketPlants.getConfig().bAllowRefills || !(hasPlant(context.getStack()))) {
            ItemStack stack = context.getStack();
            World world = context.getWorld();
            ItemStack grabbedStack = AttemptGrabBlock(stack, world, context.getBlockPos());
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
            if (hasPlant(stack) && isMature(stack)) {
                String namespace = Identifier.tryParse(getPlantBlockId(stack)).getNamespace();
                if (namespace==null) { namespace = ""; }
                if (!namespace.isEmpty() && PocketPlants.getConfig().isBlacklisted( namespace )) {
                    return TypedActionResult.pass(stack);
                }
                if (PocketPlants.getConfig().bEnableSpeedIncrease) {
                    setSpeedMultiplier(stack, getSpeedMultiplier(stack) + PocketPlants.getConfig().RateOfSpeedIncrease());
                }
                List<ItemStack> products = getPlantProductStack((ServerWorld)world, player, stack);
                //find seeds to keep this from ballooning seed stock:
                for (ItemStack product: products) {
                    if (!player.giveItemStack(product)) {
                        ItemScatterer.spawn(world, player.getX(), player.getY(), player.getZ(), product);
                    }
                }
                if (getPlantBlock(stack) instanceof KelpBlock)
                    setContainedPlantAge(stack,world.random.nextInt(24));
                else
                    setContainedPlantAge(stack, 0);
                return TypedActionResult.success(stack);
            }
        }
        return TypedActionResult.pass(stack);
    }


    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (!world.isClient()) {
            boolean randomTick = world.random.nextInt(4096) < (int)(world.getGameRules().getInt(GameRules.RANDOM_TICK_SPEED)*getSpeedMultiplier(stack));
            if (randomTick && hasPlant(stack) && !isMature(stack)) {
                growContainedPlant(stack);
            }
        }
        int status=0;
        if (hasPlant(stack)) {
            if (isMature(stack)) {
                status=3;
            }else {
                status = ((float)getContainedPlantAge(stack) / (float)getContainedPlantMaxAge(stack) > 0.5F) ? 2 : 1;
            }
        }/*
        if (isWaterPlant(stack)) {
            status+=3;
        }
        if (isFlowerPlant(stack)) {
            status+=6;
        }*/
        stack.getOrCreateTag().putInt("CustomModelData",status);
        super.inventoryTick(stack, world, entity, slot, selected);
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        if (hasPlant(stack)) {
            tooltip.add(new TranslatableText("info.pocketplants.age", getContainedPlantAge(stack), getContainedPlantMaxAge(stack)));
        }else{
            tooltip.add(new TranslatableText("info.pocketplants.empty1").formatted(Formatting.ITALIC).formatted(Formatting.GRAY));
            tooltip.add(new TranslatableText("info.pocketplants.empty2").formatted(Formatting.ITALIC).formatted(Formatting.GRAY));
        }
        super.appendTooltip(stack, world, tooltip, context);
    }

    @Override
    public Text getName(ItemStack stack) {
        Text name = new TranslatableText("item.pocketplants.terrarium");
        if (hasPlant(stack)) {
            name = new TranslatableText("info.pocketplants.nameconcat",getPlantBlock(stack).getName(),name).formatted(Formatting.GREEN);
        }
        return name;
    }
}
