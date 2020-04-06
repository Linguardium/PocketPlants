package mod.linguardium.pocketplants.items;

import li.cryx.convth.block.AbstractResourcePlant;
import mod.linguardium.pocketplants.compat.ConvenientThings;
import net.fabricmc.loader.api.FabricLoader;
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

import java.util.List;

public class PocketTerrarium extends Item {

    public PocketTerrarium(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        ItemStack stack = context.getStack();
        BlockPos pos = context.getBlockPos();
        BlockState bState = context.getWorld().getBlockState(pos);
        Block block = bState.getBlock();
        if (!hasPlant(stack)) {
            if (
                    block instanceof CropBlock ||
                    (block instanceof SugarCaneBlock && !(context.getWorld().getBlockState(pos.offset(Direction.DOWN)).getBlock() instanceof SugarCaneBlock)) ||
                    ((block instanceof KelpBlock || block instanceof KelpPlantBlock) && !(context.getWorld().getBlockState(pos.offset(Direction.DOWN)).getBlock() instanceof KelpBlock) && !(context.getWorld().getBlockState(pos.offset(Direction.DOWN)).getBlock() instanceof KelpPlantBlock))

            ) {
                if (block instanceof KelpPlantBlock) {
                    // kelp grows, Kelp plant is just the base plant which we are grabbing.
                    addPlant(stack,Registry.BLOCK.getId(Blocks.KELP).toString());
                }else {
                    addPlant(stack, Registry.BLOCK.getId(block).toString());
                }
//                setAge(stack,age);
                context.getWorld().breakBlock(pos,false);
                context.getWorld().breakBlock(pos.offset(Direction.DOWN),false);
            }
        }
        return super.useOnBlock(context);
    }

    protected Block getPlantBlock(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateSubTag("Plant");
        if (!tag.isEmpty()) {
            String blockID = tag.getString("block");
            if (!blockID.isEmpty()) {
                Block b = Registry.BLOCK.get(Identifier.tryParse(blockID));
                return b;
            }
        }
        return Blocks.AIR;
    }
    protected void addPlant(ItemStack stack, String block) {
        CompoundTag plantTag = new CompoundTag();
        plantTag.putInt("age",0);
        plantTag.putString("block",block);
        stack.putSubTag("Plant",plantTag);
    }
    public boolean isMature(ItemStack stack) {
        return (getAge(stack) >= getMaxAge(stack));
    }

    public List<ItemStack> getPlantProductStack(ServerWorld world, PlayerEntity player, ItemStack stack) {
        Block bPlant = getPlantBlock(stack);
        List<ItemStack> newStack = DefaultedList.of();
        if (bPlant instanceof CropBlock) {
            if (FabricLoader.getInstance().isModLoaded("convth") && bPlant instanceof AbstractResourcePlant) {
                newStack = (ConvenientThings.getResourcePlantDrops(world,(AbstractResourcePlant)bPlant));
            }else {
                BlockState bState = bPlant.getDefaultState().with(((CropBlock) bPlant).getAgeProperty(), getAge(stack));
                newStack = bPlant.getDroppedStacks(bState, world, player.getBlockPos(), null); //bState.getDroppedStacks(builder);
            }
        }else if(bPlant instanceof SugarCaneBlock) {
            BlockState bState = bPlant.getDefaultState().with(SugarCaneBlock.AGE, getAge(stack));
            newStack = bPlant.getDroppedStacks(bState,world,player.getBlockPos(),null);
        }else if (bPlant instanceof KelpBlock) {
            BlockState bState = bPlant.getDefaultState().with(KelpBlock.AGE, getAge(stack));
            newStack = bPlant.getDroppedStacks(bState,world,player.getBlockPos(),null);
        }
        return newStack;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (!world.isClient()) {
            if (hasPlant(stack) && isMature(stack)) {
                List<ItemStack> products = getPlantProductStack((ServerWorld)world, player, stack);
                for (ItemStack product: products) {
                    if (!player.giveItemStack(product)) {
                        ItemScatterer.spawn(world, player.getX(), player.getY(), player.getZ(), product);
                    }
                }
                if (getPlantBlock(stack) instanceof KelpBlock)
                    setAge(stack,world.random.nextInt(24));
                else
                    setAge(stack, 0);
                return TypedActionResult.success(stack);
            }
        }
        return TypedActionResult.pass(stack);
    }
    protected boolean hasPlant(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateSubTag("Plant");
        if (!tag.isEmpty()) {
            String blockID = tag.getString("block");
            return !blockID.isEmpty();
        }
        return false;
    }
    protected void growPlant(ItemStack stack) {
        if (!isMature(stack)) {
            setAge(stack, getAge(stack) + 1);
        }
    }
    protected void setAge(ItemStack stack, int age) {
        CompoundTag tag = stack.getOrCreateSubTag("Plant");
        if (!tag.isEmpty()) {
            tag.putInt("age", age);
        }
        stack.putSubTag("Plant", tag);
    }
    protected int getMaxAge(ItemStack stack) {
        Block plant = getPlantBlock(stack);
        if (plant instanceof CropBlock) {
            return ((CropBlock) plant).getMaxAge();
        }else if (plant instanceof SugarCaneBlock) {
            return 15;
        }else if (plant instanceof KelpBlock) {
            return 25;
        }
        return 0;
    }
    protected int getAge(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateSubTag("Plant");
        if (tag.isEmpty()) {
            return 0;
        }
        return tag.getInt("age");
    }
    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (!world.isClient()) {
            boolean randomTick = world.random.nextInt(4096) < world.getGameRules().getInt(GameRules.RANDOM_TICK_SPEED);
            if (randomTick && hasPlant(stack) && !isMature(stack)) {
                growPlant(stack);
            }
        }
        int status=0;
        if (hasPlant(stack)) {
            if (isMature(stack)) {
                status=3;
            }else {
                status = ((float)getAge(stack) / (float)getMaxAge(stack) > 0.5F) ? 2 : 1;
            }
        }
        if (isWaterPlant(stack)) {
            status+=3;
        }
        stack.getOrCreateTag().putInt("CustomModelData",status);
        super.inventoryTick(stack, world, entity, slot, selected);
    }
    public boolean isWaterPlant(ItemStack stack) {
        return (getPlantBlock(stack) instanceof KelpBlock);
    }
    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        if (hasPlant(stack)) {
            tooltip.add(new TranslatableText("info.pocketplants.age", getAge(stack), getMaxAge(stack)));
        }else{
            tooltip.add(new TranslatableText("info.pocketplants.empty").formatted(Formatting.ITALIC).formatted(Formatting.GRAY));
        }
        super.appendTooltip(stack, world, tooltip, context);
    }

    @Override
    public Text getName(ItemStack stack) {
        Text name = new TranslatableText("item.pocketplants.terrarium");
        if (hasPlant(stack)) {
            name = new TranslatableText("info.pocketplants.nameconcat",getPlantBlock(stack).getName(),name);
        }
        return name;
    }
}
