package com.example.examplemod.container;

import com.example.examplemod.block.ModBlocks;
import com.example.examplemod.tileentity.TestBlockTile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.IContainerProvider;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

import java.util.Optional;

public class TestBlockContainer extends Container {

    public static final ITextComponent TITLE = new TranslationTextComponent("container.examplemod.test_block");

    // slot positions
    public static final int SLOT_SPACING = 18;
    public static final int INPUT_START_X = 8;
    public static final int INPUT_START_Y = 63;
    public static final int PLAYER_INVENTORY_START_X = 8;
    public static final int PLAYER_INVENTORY_START_Y = 86;

    // slot counts
    public static final int INPUT_ROWS = 9;
    public static final int INPUT_COLUMNS = 1;
    public static final int PLAYER_BACKPACK_ROWS = 3;
    public static final int PLAYER_INVENTORY_COLUMNS = 9;
    public static final int INPUT_SLOTS = INPUT_ROWS * INPUT_COLUMNS;
    public static final int PLAYER_BACKPACK_SLOTS = PLAYER_BACKPACK_ROWS * PLAYER_INVENTORY_COLUMNS;
    public static final int PLAYER_HOTBAR_SLOTS = PLAYER_INVENTORY_COLUMNS;
    public static final int PLAYER_INVENTORY_SLOTS = PLAYER_BACKPACK_SLOTS + PLAYER_HOTBAR_SLOTS;

    // slot indices;
    public static final int FIRST_HOTBAR_SLOT = 0;
    public static final int FIRST_BACKPACK_SLOT = FIRST_HOTBAR_SLOT + PLAYER_HOTBAR_SLOTS;
    public static final int FIRST_PLAYER_SLOT = FIRST_HOTBAR_SLOT;
    public static final int FIRST_INPUT_SLOT = FIRST_BACKPACK_SLOT + PLAYER_BACKPACK_SLOTS;

    public static final int END_INPUT_SLOTS = FIRST_INPUT_SLOT + INPUT_SLOTS;
    public static final int END_PLAYER_SLOTS = FIRST_BACKPACK_SLOT + PLAYER_BACKPACK_SLOTS;


    public final Optional<TestBlockTile> testBlockTile;
    private final IWorldPosCallable usabilityTest;
    private final IItemHandler playerInventoryWrapper;
    public String instructions = "test";

    /** Container factory for opening the container clientside **/
    public static TestBlockContainer getClientContainer(int id, PlayerInventory playerInventory,
                                                        PacketBuffer data) {
        // init client inventory with dummy slots
        return new TestBlockContainer(id, playerInventory,
                BlockPos.ZERO, TestBlockTile.createClientHandler(INPUT_SLOTS), data.readUtf(), Optional.empty());
    }

    /**
     * Get the server container provider for NetworkHooks.openGui
     * @param testBlockTile The TileEntity
     * @param activationPos The position of the block that the player activated to open the container
     * @return IContainerProvider
     */
    public static IContainerProvider getServerContainer(TestBlockTile testBlockTile,
                                                        BlockPos activationPos) {
        return (id, playerInventory, serverPlayer) ->
                new TestBlockContainer(id, playerInventory,
                        activationPos, testBlockTile.itemHandler, "", Optional.of(testBlockTile));
    }

    protected TestBlockContainer(int windowId, PlayerInventory playerInventory,
                                 BlockPos pos, IItemHandler inputs, String instructions, Optional<TestBlockTile> testBlockTile) {
        super(ModContainers.TEST_BLOCK_CONTAINER.get(), windowId);
        PlayerEntity player = playerInventory.player;
        this.usabilityTest = IWorldPosCallable.create(player.level, pos);
        this.testBlockTile = testBlockTile;
        this.playerInventoryWrapper = new InvWrapper(playerInventory);
        this.instructions = instructions;

        // add input slots
        addSlotRange(inputs, 0, INPUT_START_X, INPUT_START_Y, INPUT_SLOTS, SLOT_SPACING);

        // add player inventory
        layoutPlayerInventorySlots(PLAYER_INVENTORY_START_X, PLAYER_INVENTORY_START_Y);



        /*
        if (tileEntity != null) {
            tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(h -> {
                addSlotRange(h, 0, INPUT_START_X, INPUT_START_Y, 9, SLOT_SPACING);
            });
        }
        */
    }



    @Override
    public boolean stillValid(PlayerEntity playerIn) {
        return stillValid(this.usabilityTest, playerIn, ModBlocks.TEST_BLOCK.get());
    }

    private int addSlotRange(IItemHandler handler, int index, int x, int y, int amount, int dx) {
        for (int i = 0; i < amount; i++) {
            addSlot(new SlotItemHandler(handler, index, x, y));
            x += dx;
            index++;
        }

        return index;
    }

    @Override
    public void setData(int id, int data) {
        super.setData(id, data);
    }

    private int addSlotBox(IItemHandler handler, int index, int x, int y, int horAmount, int dx, int verAmount, int dy) {
        for (int j = 0; j < verAmount; j++) {
            index = addSlotRange(handler, index, x, y, horAmount, dx);
            y += dy;
        }

        return index;
    }

    private void layoutPlayerInventorySlots(int leftCol, int topRow) {
        addSlotBox(playerInventoryWrapper, 9, leftCol, topRow, 9, 18, 3, 18);

        topRow += 58;
        addSlotRange(playerInventoryWrapper, 0, leftCol, topRow, 9, 18);
    }

    // CREDIT GOES TO: diesieben07 | https://github.com/diesieben07/SevenCommons
    // must assign a slot number to each of the slots used by the GUI.
    // For this container, we can see both the tile inventory's slots as well as the player inventory slots and the hotbar.
    // Each time we add a Slot to the container, it automatically increases the slotIndex, which means
    //  0 - 8 = hotbar slots (which will map to the InventoryPlayer slot numbers 0 - 8)
    //  9 - 35 = player inventory slots (which map to the InventoryPlayer slot numbers 9 - 35)
    //  36 - 44 = TileInventory slots, which map to our TileEntity slot numbers 0 - 8)



    @Override
    public ItemStack quickMoveStack(PlayerEntity playerIn, int index) {
        Slot sourceSlot = slots.get(index);
        if (sourceSlot == null || !sourceSlot.hasItem()) return ItemStack.EMPTY;  //EMPTY_ITEM
        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();

        // Check if the slot clicked is one of the vanilla container slots
        if (index < END_PLAYER_SLOTS) {
            // This is a vanilla container slot so merge the stack into the tile inventory
            if (!moveItemStackTo(sourceStack, FIRST_INPUT_SLOT, END_INPUT_SLOTS, false)) {
                return ItemStack.EMPTY;  // EMPTY_ITEM
            }
        } else if (index < END_INPUT_SLOTS) {
            // This is a TE slot so merge the stack into the players inventory
            if (!moveItemStackTo(sourceStack, FIRST_PLAYER_SLOT, END_PLAYER_SLOTS, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            System.out.println("Invalid slotIndex:" + index);
            return ItemStack.EMPTY;
        }
        // If stack size == 0 (the entire stack was moved) set slot contents to null
        if (sourceStack.getCount() == 0) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }
        sourceSlot.onTake(playerIn, sourceStack);
        return copyOfSourceStack;
    }
}
