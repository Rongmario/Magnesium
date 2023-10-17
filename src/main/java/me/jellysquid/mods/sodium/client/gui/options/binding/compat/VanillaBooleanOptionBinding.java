package me.jellysquid.mods.sodium.client.gui.options.binding.compat;

import me.jellysquid.mods.sodium.client.gui.options.binding.OptionBinding;
import net.minecraft.client.settings.GameSettings;

public class VanillaBooleanOptionBinding implements OptionBinding<GameSettings, Boolean> {
    private boolean option;

    public VanillaBooleanOptionBinding(boolean option) {
        this.option = option;
    }

    @Override
    public void setValue(GameSettings storage, Boolean value) {
        this.option = value.booleanValue();
    }

    @Override
    public Boolean getValue(GameSettings storage) {
        return option;
    }
}
