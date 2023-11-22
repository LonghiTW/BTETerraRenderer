package com.mndk.bteterrarenderer.mod.client.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import lombok.experimental.UtilityClass;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;

@UtilityClass
public class CommandsRegisterer {

    public void register() {
        ClientCommandManager.DISPATCHER.register(
                LiteralArgumentBuilder.<FabricClientCommandSource>literal("btr")
                        .then(getToggleSubcommand())
        );
    }

    private LiteralCommandNode<FabricClientCommandSource> getToggleSubcommand() {
        return LiteralArgumentBuilder.<FabricClientCommandSource>literal("toggle")
                .executes(new ToggleRenderingCommand())
                .build();
    }

}