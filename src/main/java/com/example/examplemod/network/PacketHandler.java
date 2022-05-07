package com.example.examplemod.network;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.network.packet.PrintVariableCard;
import com.example.examplemod.network.packet.UpdateInstructions;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ExampleMod.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void init() {
        int id = 0;
        INSTANCE.registerMessage(id++, PrintVariableCard.class, PrintVariableCard::encode,
                PrintVariableCard::decode, PrintVariableCard::handle);
        INSTANCE.registerMessage(id++, UpdateInstructions.class, UpdateInstructions::encode,
                UpdateInstructions::decode, UpdateInstructions::handle);
    }
}
