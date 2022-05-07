package com.example.examplemod.tileentity;

import com.example.examplemod.ExampleMod;
import com.google.common.collect.Lists;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.item.IOperatorVariableFacade;
import org.cyclops.integrateddynamics.api.item.IValueTypeVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHandlerRegistry;
import org.cyclops.integrateddynamics.api.logicprogrammer.ILogicProgrammerElement;
import org.cyclops.integrateddynamics.api.logicprogrammer.ILogicProgrammerElementType;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeBoolean;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.item.OperatorVariableFacade;
import org.cyclops.integrateddynamics.core.item.ValueTypeVariableFacade;
import org.cyclops.integrateddynamics.core.logicprogrammer.LogicProgrammerElementTypes;
import org.cyclops.integrateddynamics.core.logicprogrammer.OperatorLPElement;
import org.cyclops.integrateddynamics.core.persist.world.LabelsWorldStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TestBlockTile extends TileEntity {
    public String instructions = "";
    public final ItemStackHandler itemHandler = createHandler(9);
    private final LazyOptional<IItemHandler> handler = LazyOptional.of(() -> itemHandler);

    public TestBlockTile(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    public TestBlockTile() {
        this(ModTileEntities.TEST_BLOCK_TILE.get());
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return super.getUpdateTag();
    }

    @Override
    public void load(BlockState state, CompoundNBT nbt) {
        ExampleMod.LOGGER.info("read()");

        this.itemHandler.deserializeNBT(nbt.getCompound("inv"));
        this.instructions = nbt.getString("instructions");

        super.load(state, nbt);
    }

    @Override
    public CompoundNBT save(CompoundNBT compound) {
        ExampleMod.LOGGER.info("write()");

        compound.put("inv", this.itemHandler.serializeNBT());
        compound.putString("instructions", this.instructions);

        return super.save(compound);
    }

    protected static class OperatorVariableFacadeFactory implements IVariableFacadeHandlerRegistry.IVariableFacadeFactory<IOperatorVariableFacade> {

        private final IOperator operator;
        private final int[] variableIds;

        public OperatorVariableFacadeFactory(IOperator operator, int[] variableIds) {
            this.operator = operator;
            this.variableIds = variableIds;
        }

        @Override
        public IOperatorVariableFacade create(boolean generateId) {
            return new OperatorVariableFacade(generateId, operator, variableIds);
        }

        @Override
        public IOperatorVariableFacade create(int id) {
            return new OperatorVariableFacade(id, operator, variableIds);
        }
    }

    protected static class ValueTypeVariableFacadeFactory implements IVariableFacadeHandlerRegistry.IVariableFacadeFactory<IValueTypeVariableFacade> {

        private final IValueType valueType;
        private final IValue value;

        public ValueTypeVariableFacadeFactory(IValueType valueType, IValue value) {
            this.valueType = valueType;
            this.value = value;
        }

        public ValueTypeVariableFacadeFactory(IValue value) {
            this(value.getType(), value);
        }

        @Override
        public IValueTypeVariableFacade create(boolean generateId) {
            return new ValueTypeVariableFacade(generateId, valueType, value);
        }

        @Override
        public IValueTypeVariableFacade create(int id) {
            return new ValueTypeVariableFacade(id, valueType, value);
        }
    }

    public void setInstructions(String newInstructions) {
        this.instructions = newInstructions;
        setChanged();
    }

    public void test() {
        List<String> valueTypes = ValueTypes.REGISTRY.getValueTypes().stream()
                .map(valueType -> valueType.getUniqueName().getPath()).collect(Collectors.toList());
        List<String> operators = Operators.REGISTRY.getOperators().stream()
                .map(valueType -> valueType.getUniqueName().getPath()).collect(Collectors.toList());
        HashMap<String, Integer> idList = new HashMap<String, Integer>();

        IVariableFacadeHandlerRegistry registry = IntegratedDynamics
                ._instance.getRegistryManager()
                .getRegistry(IVariableFacadeHandlerRegistry.class);


        String testString = "test1 = boolean true\ntest2 = boolean false\ntest3 = logical_and test1 test2";
        Iterator<String> step = Arrays.stream(instructions.split("\n")).iterator();


        for (int slot = 1; slot < this.itemHandler.getSlots(); slot++) {
            if (this.itemHandler.getStackInSlot(0).isEmpty()
                    || !step.hasNext()) { return; }
            if (!this.itemHandler.getStackInSlot(slot).isEmpty()) { continue; }


            Pattern pattern = Pattern.compile("^(?<name>\\S+) = (?<operator>\\S+)(?<arguments>(?: \\S+)+)$");
            Matcher matcher = pattern.matcher(step.next());

            if (!matcher.find()) {
                ExampleMod.LOGGER.info("Invalid input string");
                return;
            }

            String name = matcher.group("name");
            String operatorName = matcher.group("operator");
            String[] arguments = matcher.group("arguments").trim().split(" ");


            //String[] instruction = step.next().split("=");
            //String name = instruction.length > 0 ? instruction[0].trim() : null;
            //String[] tmp = instruction[name == null ? 0 : 1].trim().split(" ");
            //String operatorName = tmp[0];
            //String[] inputs = Arrays.copyOfRange(tmp, 1, tmp.length);

            ItemStack itemStack = this.itemHandler.extractItem(0, 1, false);



            if (valueTypes.contains(operatorName)) {
                IValueType valueType = ValueTypes.REGISTRY.getValueType(
                        new ResourceLocation("integrateddynamics", operatorName));
                IValue value = ValueTypeBoolean.ValueBoolean.of(Boolean.parseBoolean(arguments[0]));

                itemStack = registry.writeVariableFacadeItem(true, itemStack, ValueTypes.REGISTRY,
                        new ValueTypeVariableFacadeFactory(valueType, value),
                        null, null);
            } else if (operators.contains(operatorName)) {
                IOperator operator = Operators.REGISTRY.getOperator(
                        new ResourceLocation("integrateddynamics", operatorName));

                int[] variableIds = new int[arguments.length];
                for (int i = 0; i < arguments.length; i++) {
                    Integer id = idList.get(arguments[i]);

                    if (id == null) {
                        ExampleMod.LOGGER.info("Unknown id");
                        return;
                    }

                    variableIds[i] = id;
                }

                itemStack = registry.writeVariableFacadeItem(true, itemStack, Operators.REGISTRY,
                        new OperatorVariableFacadeFactory(operator, variableIds),
                        null, null);
            }

            if (name != null && itemStack.getItem() == RegistryEntries.ITEM_VARIABLE) {
                IVariableFacade variableFacade = RegistryEntries.ITEM_VARIABLE.getVariableFacade(itemStack);
                if (variableFacade.isValid()) {
                    int id = variableFacade.getId();
                    idList.put(name, id);
                    LabelsWorldStorage.getInstance(IntegratedDynamics._instance).put(id, name);
                }
            }

            this.itemHandler.insertItem(slot, itemStack, false);
        }
    }

    private ItemStackHandler createHandler(int size) {
        return new ItemStackHandler(size) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }

            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                switch (slot) {
                    default: return stack.getItem() == RegistryEntries.ITEM_VARIABLE;
                }
            }

            @Override
            public int getSlotLimit(int slot) {
                switch (slot) {
                    case 0: return 64;
                    default: return 1;
                }
            }

            @Nonnull
            @Override
            public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
                if(!isItemValid(slot, stack)) {
                    return stack;
                }

                return super.insertItem(slot, stack, simulate);
            }
        };
    }

    public static ItemStackHandler createClientHandler(int size) {
        return new ItemStackHandler(size) {

            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                switch (slot) {
                    default: return stack.getItem() == RegistryEntries.ITEM_VARIABLE;
                }
            }

            @Override
            public int getSlotLimit(int slot) {
                switch (slot) {
                    case 0: return 64;
                    default: return 1;
                }
            }

            @Nonnull
            @Override
            public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
                if(!isItemValid(slot, stack)) {
                    return stack;
                }

                return super.insertItem(slot, stack, simulate);
            }
        };
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return handler.cast();
        }

        return super.getCapability(cap, side);
    }
}
