package com.mndk.bteterrarenderer.mixin.minecraft;

import com.mndk.bteterrarenderer.core.util.i18n.I18nManager;
import lombok.experimental.UtilityClass;
import net.minecraft.client.resources.I18n;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@UtilityClass
@Mixin(value = I18nManager.class, remap = false)
public class I18nManagerMixin12 {
    @Overwrite
    public String format(String key, Object... parameters) {
        return I18n.format(key);
    }
}
