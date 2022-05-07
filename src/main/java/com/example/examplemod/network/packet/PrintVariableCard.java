package com.example.examplemod.network.packet;

import com.example.examplemod.container.TestBlockContainer;
import com.example.examplemod.tileentity.TestBlockTile;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PrintVariableCard {
    public PrintVariableCard() {
    }

    public void encode(PacketBuffer buffer) {
    }

    public static PrintVariableCard decode(PacketBuffer buffer) {
        return new PrintVariableCard();
    }

    public static void handle(PrintVariableCard message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            Container container = context.getSender().containerMenu;
            if (container instanceof TestBlockContainer) {
                ((TestBlockContainer) container).testBlockTile.ifPresent(TestBlockTile::test);
            }
        });
        context.setPacketHandled(true);
    }
}
