package com.example.examplemod.network.packet;

import com.example.examplemod.container.TestBlockContainer;
import com.example.examplemod.tileentity.TestBlockTile;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class UpdateInstructions {
    public String instruction;

    public UpdateInstructions() {
    }

    public UpdateInstructions(String instruction) {
        this.instruction = instruction;
    }

    public void encode(PacketBuffer buffer) {
        buffer.writeUtf(this.instruction);
    }

    public static UpdateInstructions decode(PacketBuffer buffer) {
        return new UpdateInstructions(buffer.readUtf(32500));
    }

    public static void handle(UpdateInstructions message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            Container container = context.getSender().containerMenu;
            if (container instanceof TestBlockContainer) {
                ((TestBlockContainer) container).testBlockTile.ifPresent(tile -> {
                    tile.setInstructions(message.instruction);
                });
            }
        });
        context.setPacketHandled(true);
    }
}
