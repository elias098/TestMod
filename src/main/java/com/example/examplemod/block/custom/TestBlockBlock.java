package com.example.examplemod.block.custom;

import com.example.examplemod.container.TestBlockContainer;
import com.example.examplemod.tileentity.ModTileEntities;
import com.example.examplemod.tileentity.TestBlockTile;
import io.netty.buffer.Unpooled;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.IContainerProvider;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;

import net.minecraft.block.AbstractBlock.Properties;

public class TestBlockBlock extends Block {
    public TestBlockBlock(Properties properties) {
        super(properties);
    }


    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos,
                                             PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        TileEntity tileEntity = world.getBlockEntity(pos);
        if (tileEntity instanceof TestBlockTile) {
            if (player instanceof ServerPlayerEntity) {
                ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
                TestBlockTile testBlockTile = ((TestBlockTile) tileEntity);

                IContainerProvider provider =
                        TestBlockContainer.getServerContainer(testBlockTile, pos);
                INamedContainerProvider namedProvider =
                        new SimpleNamedContainerProvider(provider, TestBlockContainer.TITLE);

                NetworkHooks.openGui(serverPlayer, namedProvider, packetBuffer ->
                        packetBuffer.writeUtf(testBlockTile.instructions));
            }

            return ActionResultType.SUCCESS;
        }

        return super.use(state, world, pos, player, handIn, hit);






        /*
        if(!world.isRemote()) {
            TileEntity tileEntity = world.getTileEntity(pos);

            if (!player.isCrouching()) {
                if (tileEntity instanceof TestBlockTile) {
                    INamedContainerProvider containerProvider = createContainerProvider(world, pos);

                    NetworkHooks.openGui(((ServerPlayerEntity) player), containerProvider, tileEntity.getPos());
                } else {
                    throw new IllegalStateException("Our container provider is missing!");
                }
            } else {

            }
        }


        return ActionResultType.SUCCESS;

         */
    }

//    private INamedContainerProvider createContainerProvider(World worldIn, BlockPos pos) {
//        return new INamedContainerProvider() {
//            @Override
//            public ITextComponent getDisplayName() {
//                return new TranslationTextComponent("screen.examplemod.test_block");
//            }
//
//            @Nullable
//            @Override
//            public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
//                TileEntity tileEntity = worldIn.getTileEntity(pos);
//                if (tileEntity instanceof TestBlockTile) {
//                    if (playerEntity instanceof ServerPlayerEntity) {
//                        ServerPlayerEntity serverPlayer = (ServerPlayerEntity) playerEntity;
//                        TestBlockTile testBlockTile = ((TestBlockTile) tileEntity);
//
//                    }
//                }
//
//
//
//                return new TestBlockContainer(i, playerInventory, pos, , worldIn, );
//            }
//        };
//    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return ModTileEntities.TEST_BLOCK_TILE.get().create();
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }
}
