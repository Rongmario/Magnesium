package me.jellysquid.mods.sodium.compat.client.gui;

import me.jellysquid.mods.sodium.client.util.Dim2i;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.text.TextComponentTranslation;
import org.jetbrains.annotations.NotNull;

public class EasyButton extends GuiButton {

    @NotNull
    private final Runnable action;

    public EasyButton(int buttonId, Dim2i dim2i, TextComponentTranslation buttonText, Runnable action) {
        super(buttonId, dim2i.getOriginX(), dim2i.getOriginY(), buttonText.getFormattedText());
        this.action = action;
    }

    public EasyButton(int buttonId, int x, int y, String buttonText, Runnable action) {
        super(buttonId, x, y, buttonText);
        this.action = action;
    }

    public EasyButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText, Runnable action) {
        super(buttonId, x, y, widthIn, heightIn, buttonText);
        this.action = action;
    }


    public Runnable getAction() {
        return action;
    }
}
