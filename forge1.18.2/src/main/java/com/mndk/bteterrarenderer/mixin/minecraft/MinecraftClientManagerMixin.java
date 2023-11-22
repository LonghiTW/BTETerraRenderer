package com.mndk.bteterrarenderer.mixin.minecraft;

import com.mndk.bteterrarenderer.core.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.core.util.minecraft.MinecraftClientManager;
import lombok.experimental.UtilityClass;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.sounds.SoundEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@UtilityClass
@Mixin(value = MinecraftClientManager.class, remap = false)
public class MinecraftClientManagerMixin {

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public double getPlayerRotationYaw() {
        LocalPlayer player = Minecraft.getInstance().player;
        return player != null ? player.getYRot() : 0;
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public void sendErrorMessageToChat(String message) {
        LocalPlayer player = Minecraft.getInstance().player;
        if(player != null) {
            String componentString = "§c[" + BTETerraRendererConstants.NAME + "] " + message;
            player.sendMessage(new TextComponent(componentString), Util.NIL_UUID);
        }
        BTETerraRendererConstants.LOGGER.error(message);
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public void playClickSound() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(
                SoundEvents.UI_BUTTON_CLICK, 1.0f
        ));
    }

//    /** @author m4ndeokyi
//     *  @reason mixin overwrite */
//    @Overwrite
//    public int chatOpenKeyCode() {
//        return Minecraft.getInstance().options.keyChat.getKey().getValue();
//    }
//
//    /** @author m4ndeokyi
//     *  @reason mixin overwrite */
//    @Overwrite
//    public int commandOpenKeyCode() {
//        return Minecraft.getInstance().options.keyCommand.getKey().getValue();
//    }
}