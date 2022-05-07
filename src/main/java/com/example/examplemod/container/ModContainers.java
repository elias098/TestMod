package com.example.examplemod.container;

import com.example.examplemod.ExampleMod;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModContainers {

    public static DeferredRegister<ContainerType<?>> CONTAINERS
            = DeferredRegister.create(ForgeRegistries.CONTAINERS, ExampleMod.MOD_ID);

    public static final RegistryObject<ContainerType<TestBlockContainer>> TEST_BLOCK_CONTAINER
            = CONTAINERS.register("test_block_container",

            () -> IForgeContainerType.create((TestBlockContainer::getClientContainer)));

    public static void register(IEventBus eventBus) {
        CONTAINERS.register(eventBus);
    }
}
