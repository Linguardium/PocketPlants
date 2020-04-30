package mod.linguardium.pocketplants.api;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Property;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.apache.logging.log4j.Level;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

import static mod.linguardium.pocketplants.PocketPlants.log;

public class PlantTag extends CompoundTag {
    public static final int FAKE_MAXAGE = 5;
    public PlantTag() {
        this(new CompoundTag(), new CompoundTag(),new CompoundTag());
    }
    public PlantTag(CompoundTag blockState, CompoundTag blockEntity, CompoundTag soilState) {
        super();
        this.put("blockState",blockState);
        this.put("blockEntity", blockEntity);
        this.put("soilState",soilState);
    }

    public static PlantTag fromTag(CompoundTag tag) {
        if (PlantTag.validate(tag)) {
            PlantTag pTag = new PlantTag();
            pTag.copyFrom(tag);
            return pTag;
        }else{
            log(Level.ERROR, "Malformed tag provided to PlantTag. Missing blockState or blockEntity tag in PlantTag(CompoundTag) constructor");
            return null;
        }
    }

    /***
     *       Requires a CompoundTag with valid plant data inside a CompoundTag element keyed as "Plant"
     ***/
    public static PlantTag fromOldTag(World world, CompoundTag tag) {
        if (validate(tag)) {
            return (PlantTag)tag.copy();
        }
        if (tag.contains("Plant")) {
            CompoundTag plantData = tag.getCompound("Plant");
            Block block = Registry.BLOCK.get(new Identifier(plantData.getString("block")));
            if (block != Blocks.AIR) {
                PlantTag pTag = new PlantTag();
                CompoundTag fData = new CompoundTag();
                BlockState state = block.getDefaultState();
                int age = Integer.valueOf(plantData.getString("age"));
                Integer maxAge=0;
                List<Property> propertyList = state.getProperties().stream().filter((property) -> property.getName().equals("age")).collect(Collectors.toList());
                if (propertyList.size()>0) {
                    state = state.with(propertyList.get(0), age);
                    maxAge = (Integer)propertyList.get(0).getValues().toArray()[propertyList.get(0).getValues().size()-1];
                }else{
                    fData.putInt("age",age);
                    maxAge = 5;
                }
                fData.putInt("maxAge",maxAge);
                pTag.put("blockState", NbtHelper.fromBlockState(state));
                pTag.put("fakeData",fData);
                BlockEntity be=null;
                if (block instanceof BlockEntityProvider && world != null) {
                    be = ((BlockEntityProvider) block).createBlockEntity(world);
                }
                CompoundTag beTag = new CompoundTag();
                if (be != null) {
                    beTag = be.toTag(beTag);
                }
                pTag.put("blockEntity",beTag);
                pTag.put("soilState",NbtHelper.fromBlockState(Blocks.FARMLAND.getDefaultState().with(FarmlandBlock.MOISTURE,7)));
                return pTag;
            }else{
                log(Level.ERROR, "Malformed tag provided to PlantTag: Cannot find block [ "+plantData.get("block")+" ] in Registry.BLOCK in fromOldTag()" );
                return null;
            }
        }else{
            log(Level.ERROR, "Malformed tag provided to PlantTag: \"Plant\" key missing from Old Compound Tag in fromOldTag()" );
            return null;
        }
    }

    /***
     *       determines if the necessary container tags exist within a CompoundTag
     ***/
    public static boolean validate(CompoundTag tag) {
        return (tag.contains("blockEntity") && tag.contains("blockState") && tag.contains("soilState"));
    }

    /***
     *       Requires a valid, in-world blockstate in order to read BlockEntity data
     ***/
    public PlantTag(World world, BlockPos pos, BlockState state) {
        super();
        if (state == null || world == null || pos == null) {
            throw(new NullPointerException("Null position, world, or block settings"));
        }
        CompoundTag blockEntityTag = new CompoundTag();
        BlockEntity blockEntity = null;
        if (state.getBlock().hasBlockEntity()) {
            blockEntity = world.getBlockEntity(pos);
            if (blockEntity == null) {
                throw(new NullPointerException("Block has entity but no entity is found in world"));
            }
            blockEntityTag = blockEntity.toTag(blockEntityTag);
            Registry.BLOCK_ENTITY_TYPE.getId(blockEntity.getType()).toString();
        }
        CompoundTag fData = new CompoundTag();
        Integer maxAge=0;
        List<Property> propertyList = state.getProperties().stream().filter((property) -> property.getName().equals("age")).collect(Collectors.toList());
        if (propertyList.size()>0) {
            maxAge = (Integer)propertyList.get(0).getValues().toArray()[propertyList.get(0).getValues().size()-1];
        }else{
            fData.putInt("age",0);
            maxAge = 5;
        }
        fData.putInt("maxAge",maxAge);
        this.put("blockState",NbtHelper.fromBlockState(state));
        this.put("fakeData",fData);
        this.put("blockEntity", blockEntityTag);
        this.put("soilState",NbtHelper.fromBlockState(world.getBlockState(pos.offset(Direction.DOWN))));
    }



    /***
     *  Returns an Upcasted copy of this PlantTag
     ***/
    public CompoundTag toTag() {
        return (CompoundTag)(this.copy());
    }


    /***
     *  Read the BlockState from the PlantTag
     ***/
    public BlockState getBlockState() {
        if (!this.getCompound("blockState").isEmpty())
            return NbtHelper.toBlockState(this.getCompound("blockState"));
        return null;
    }
    public BlockState getSoilState() {
        if (this.getCompound("soilState").isEmpty()) {
            this.put("soilState",NbtHelper.fromBlockState(Blocks.FARMLAND.getDefaultState().with(FarmlandBlock.MOISTURE,7)));
        }
        return NbtHelper.toBlockState(this.getCompound("soilState"));
    }

    /***
     *  Reads the BlockState from the PlantTag
     *  Then reads the block from the BlockState
     ***/
    public Block getBlock() {
        BlockState state = this.getBlockState();
        if (state != null) {
            return state.getBlock();
        }
        return null;
    }

    /***
     *  Reads the BlockEntity from the PlantTag
     *  Then reads the block from the BlockState
     ***/
    public BlockEntity getBlockEntity() {
        if (!this.getCompound("blockEntity").isEmpty()) {
            Block b = this.getBlock();
            if (b instanceof BlockEntityProvider) {
                try {
                    BlockEntity be = ((BlockEntityProvider) b).createBlockEntity(null);
                    if (be!=null) { // could just let the catch-all get this, but its a good habit
                        be.fromTag(this.getCompound("blockEntity"));
                    }
                    return be;
                } catch (Exception ignored) { }

            }
        }
        return null;
    }

    /***
     *  Attempts to call the Block object's "isMature" method via reflection.
     *  on failure, attempts to read the custom MAX_AGE and AGE properties
     *  provided by this mod
     ***/
    public boolean isMature() {
        BlockState state = this.getBlockState();
        if (state != null) {
            try { // Let's...get...dangerous.
                Method reflected$isMature = state.getBlock().getClass().getDeclaredMethod("isMature", BlockState.class);
                reflected$isMature.setAccessible(true); // just in case
                return (Boolean)reflected$isMature.invoke(state.getBlock(), state);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                return fake_isMature(state);
            }
        }
        return false;
    }
    public void grow() {
        this.setAgePropertyValue(this.getAgePropertyValue()+1);
    }
    /***
     *  Attempts to call the BlockState object's "getAge" method via reflection.
     *  on failure, attempts to read the custom AGE property provided by this mod
     ***/
    public int getAge(BlockState state) {
        if (state != null) {
            return getAgePropertyValue();
/*            try { // Let's...get...dangerous.
                Method reflected$getAge = state.getBlock().getClass().getDeclaredMethod("getAge", BlockState.class);
                reflected$getAge.setAccessible(true); // just in case
                return (Integer)reflected$getAge.invoke(state.getBlock(), state);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                return fake_getAge();
            }*/
        }
        return 0;
    }

    /***
     *  Attempts to call the Block object's "getMaxAge" method via reflection.
     *  on failure, attempts to read the AGE property and get the max value
     *  and on failing that, gets the max AGE provided by this mod
     ***/
    public int getMaxAge() {
        if (!this.contains("fakeData")) {
            CompoundTag fData = new CompoundTag();
            fData.putInt("age",0);
            fData.putInt("maxAge",FAKE_MAXAGE);
            this.put("fakeData",fData);
        }
        return this.getCompound("fakeData").getInt("maxAge");
    }

    /***
     *  Attempts to call the Block object's "getMaxAge" method via reflection.
     *  on failure, attempts to read the AGE property and get the max value
     *  and on failing that, gets the max AGE provided by this mod
     ***/
/*    public int getMaxAge(BlockState state) {
        if (state != null) {
            try { // Let's...get...dangerous.
                Method reflected$isMature = state.getBlock().getClass().getDeclaredMethod("getMaxAge");
                reflected$isMature.setAccessible(true); // just in case
                Integer maxAge = (Integer)reflected$isMature.invoke(state.getBlock());
                if (maxAge != null)
                    return maxAge;

            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                Property<Integer> prop = getAgeProperty(state.getBlock());
                Integer maxAge = (Integer)prop.getValues().toArray()[prop.getValues().size()-1];
                if (maxAge != null)
                    return maxAge;
            }
        }
        return 0;
    }*/
    /***
     *  Attempts to read the Tagged BlockState object's AGE property and get the largest value
     *  on failure, attempts to read the custom AGE property provided by this mod
     ***/
    public int getAge() {
        BlockState state = this.getBlockState();
        return getAge(state);
    }


    /***
     *  Attempts to call the Block object's "getAgeProperty" method via reflection.
     *  on failure, returns ModBlockProperties.AGE (AGE_7) provided by this mod
     ***/
/*    private static IntProperty getAgeProperty(Block block) {
        if (block != null) {
            try {
                Method reflected$getAgeProperty = block.getClass().getDeclaredMethod("getAgeProperty");

                return (IntProperty)reflected$getAgeProperty.invoke(block);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                try {
                    Field reflected$ageProperty = block.getClass().getDeclaredField("AGE");
                    reflected$ageProperty.setAccessible(true); // just in case;
                    return (IntProperty)reflected$ageProperty.get(block);
                } catch (NoSuchFieldException | IllegalAccessException ex) {

                }
            }
        }
        return null;
    }
*/
    /***
     *  Calls the static Block.getDroppedStacks(BlockState,ServerWorld,BlockPos,BlockEntity)
     *  This will go through the process to create a LootContext.Builder finally calling
     *  the Block's instanced getDroppedStacks(BlockState, LootContext.Builder)
     *  normally this gets the loot table and supplies a list of itemstacks, but doesn't have to
     ***/
    public List<ItemStack> getPlantProductStack(ServerWorld world, BlockPos pos) {
        List<ItemStack> newStack = DefaultedList.of();
        Block bPlant = this.getBlock();
        Item seedItem = null;

        if (bPlant instanceof CropBlock) {
            seedItem = bPlant.asItem();
        }else if (bPlant == null) {
            return newStack;
        }
        newStack = Block.getDroppedStacks(this.getBlockState(),world,pos,this.getBlockEntity());

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


    public void setBlockState(BlockState state) {
        if (state != null) {
            this.put("blockState",NbtHelper.fromBlockState(state));
        }
    }
    public void resetBlockStateAge() {
        setAgePropertyValue(0);
        /*CompoundTag blockStateTag = this.getCompound("blockState");

        this.put("blockState",NbtHelper.fromBlockState(this.getBlockState().with(PlantTag.getAgeProperty(this.getBlock()),0)));*/
    }
    public Identifier getBlockId() {
        CompoundTag blockStateTag = this.getCompound("blockState");
        if (blockStateTag.contains("Name")) {
            return new Identifier(blockStateTag.getString("Name"));
        }
        return Registry.BLOCK.getDefaultId();
    }
    @Environment(EnvType.CLIENT)
    public List<Text> getTooltips() {
        BlockState bs=this.getBlockState();
        List<Text> tooltips = DefaultedList.of();
        tooltips.add(new TranslatableText("info.terrarium.contains", new TranslatableText(this.getBlockTranslationKey())));
        bs.getBlock().buildTooltip(null,new TerrariumBlockView(this),null,null);
        return tooltips;
    }
    public String getBlockTranslationKey() {
        Identifier blockId = this.getBlockId();
        return Registry.BLOCK.get(blockId).getTranslationKey();
    }
    public String getPropertyByName(String name) {
        CompoundTag tag = this.getCompound("blockState");
        if (tag.contains("Properties")) {
            CompoundTag properties = tag.getCompound("Properties");
            return properties.getString(name);
        }
        return "";
    }
    public String setPropertyByName(String name, Object value) {
        CompoundTag tag = this.getCompound("blockState");
        if (tag.contains("Properties")) {
            CompoundTag properties = tag.getCompound("Properties");
            properties.putString(name,value.toString());
        }
        return "";
    }
    private boolean fake_isMature(BlockState state) {
        Integer max_age = this.getMaxAge();
        Integer age = this.getAge(state);
        return (max_age != 0 && age >= max_age);
    }
    private int fake_getAge() {
        if (!this.contains("fakeData")) {
            CompoundTag fData = new CompoundTag();
            fData.putInt("age",0);
            fData.putInt("maxAge",5);
            this.put("fakeData",fData);
        }
        return this.getCompound("fakeData").getInt("age");
    }
    private void fake_setAge(int age) {
        CompoundTag fData;
        if (!this.contains("fakeData")) {
            fData = new CompoundTag();
            fData.putInt("maxAge", 5);
        }else {
            fData = this.getCompound("fakeData");
        }
        fData.putInt("age",age);
        this.put("fakeData",fData);
    }
    private void setAgePropertyValue(int age) {
        Integer max = getMaxAge();
        if (age > max)
            age = max;
        CompoundTag bsTag = this.getCompound("blockState");
        CompoundTag propTag = bsTag.getCompound("Properties");
        if (propTag.contains("age")) {
            propTag.putString("age",String.valueOf(age));
            bsTag.put("Properties",propTag);
            this.put("blockState",bsTag);
        }else{
            fake_setAge(age);
        }
    }
    private int getAgePropertyValue() {
        CompoundTag bsTag = this.getCompound("blockState");
        CompoundTag propTag = bsTag.getCompound("Properties");
        if (propTag.contains("age",8)) {
            return Integer.parseInt(propTag.getString("age"));
        }else if(propTag.contains("age", 3)) { //(int tag)
            int age = propTag.getInt("age");
            propTag.remove("age");
            propTag.putString("age",String.valueOf(age));
            bsTag.put("Properties",propTag);
            this.put("blockState",bsTag);
            return age;
        }else{
            return fake_getAge();
        }
    }

}
