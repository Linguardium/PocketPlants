package mod.linguardium.pocketplants.impl;

import mod.linguardium.pocketplants.api.PlantTag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.math.Direction;

/**
 * A simple {@code Inventory} implementation with only default methods + an item list getter.
 *
 * Originally by Juuz
 */
public interface TerrariumInventory extends SidedInventory {
    /**
     * Gets the item list of this inventory.
     * Must return the same instance every time it's called.
     */
    DefaultedList<ItemStack> getItems();
    public PlantTag getPlantTag();
    public void setPlantTag(PlantTag pTag);
    // Inventory
    /**
     * Returns the inventory size.
     */
    @Override
    default int getInvSize() {
        return getItems().size();
    }
    /**
     * @return true if this inventory has only empty stacks, false otherwise
     */
    @Override
    default boolean isInvEmpty() {
        for (int i = 0; i < getInvSize(); i++) {
            ItemStack stack = getInvStack(i);
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }
    /**
     * Gets the item in the slot.
     */
    @Override
    default ItemStack getInvStack(int slot) {
        return getItems().get(slot);
    }
    /**
     * Takes a stack of the size from the slot.
     * <p>(default implementation) If there are less items in the slot than what are requested,
     * takes all items in that slot.
     */
    @Override
    default ItemStack takeInvStack(int slot, int count) {
        ItemStack result = Inventories.splitStack(getItems(), slot, count);
        if (!result.isEmpty()) {
            markDirty();
        }
        return result;
    }
    /**
     * Removes the current stack in the {@code slot} and returns it.
     */
    @Override
    default ItemStack removeInvStack(int slot) {
        ItemStack retval = Inventories.removeStack(getItems(), slot);
        if (!retval.isEmpty()) {
            markDirty();
        }
        return retval;
    }
    /**
     * Replaces the current stack in the {@code slot} with the provided stack.
     * <p>If the stack is too big for this inventory ({@link Inventory#getInvMaxStackAmount()}),
     * it gets resized to this inventory's maximum amount.
     */
    @Override
    default void setInvStack(int slot, ItemStack stack) {
        getItems().set(slot, stack);
        if (stack.getCount() > getInvMaxStackAmount()) {
            stack.setCount(getInvMaxStackAmount());
        }
    }
    /**
     * Clears {@linkplain #getItems() the item list}}.
     */
    @Override
    default void clear() {
        getItems().clear();
        markDirty();
    }
    default void ifEmptyResetAge() {
        if (isInvEmpty()) {
            PlantTag pTag = getPlantTag();
            if (pTag != null) {
                if (pTag.isMature()) {
                    pTag.resetBlockStateAge();
                    setPlantTag(pTag);
                }
            }
        }
    }
    @Override
    default void markDirty() {

    }
    @Override
    default boolean canPlayerUseInv(PlayerEntity player) {
        return true;
    }
    default boolean insertIntoInventory(ItemStack stack) {
        if (stack.isEmpty()) {
            return true;
        }
        for (int i=0; i<getInvSize();i++) {
            ItemStack slotInv = getInvStack(i);
            if (slotInv.isEmpty()) {
                setInvStack(i,stack);
                return true;
            }else if ( slotInv.getItem().equals(stack.getItem()) && slotInv.getCount() < slotInv.getMaxCount()) {
                int max = slotInv.getMaxCount()-slotInv.getCount();
                getInvStack(i).increment(stack.split(max).getCount());
                return insertIntoInventory(stack);
            }
        }
        return false;
    }

    @Override
    default int[] getInvAvailableSlots(Direction side) {
        return new int[]{0,1,2,3};
    }

    @Override
    default boolean canInsertInvStack(int slot, ItemStack stack, Direction dir) {
        return false;
    }

    @Override
    default boolean canExtractInvStack(int slot, ItemStack stack, Direction dir) {
        return true;
    }
}