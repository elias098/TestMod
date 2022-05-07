package com.example.examplemod.tileentity;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.block.ModBlocks;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModTileEntities {

    public static DeferredRegister<TileEntityType<?>> TILE_ENTITIES =
            DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, ExampleMod.MOD_ID);

    public static RegistryObject<TileEntityType<TestBlockTile>> TEST_BLOCK_TILE =
            TILE_ENTITIES.register("test_block_tile", () -> TileEntityType.Builder.of(
                    TestBlockTile::new, ModBlocks.TEST_BLOCK.get()).build(null));

    public static void register(IEventBus eventBus) {
        TILE_ENTITIES.register(eventBus);
    }

}
