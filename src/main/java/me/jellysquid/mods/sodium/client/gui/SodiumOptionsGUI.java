package me.jellysquid.mods.sodium.client.gui;

import me.jellysquid.mods.sodium.client.SodiumClientMod;
import me.jellysquid.mods.sodium.client.gui.options.Option;
import me.jellysquid.mods.sodium.client.gui.options.OptionFlag;
import me.jellysquid.mods.sodium.client.gui.options.OptionPage;
import me.jellysquid.mods.sodium.client.gui.options.control.ControlElement;
import me.jellysquid.mods.sodium.client.gui.options.storage.OptionStorage;
import me.jellysquid.mods.sodium.client.util.Dim2i;
import me.jellysquid.mods.sodium.compat.client.gui.EasyButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiVideoSettings;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentTranslation;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;

public class SodiumOptionsGUI extends GuiScreen {
    private final List<OptionPage> pages = new ArrayList<>();

    private final List<ControlElement<?>> controls = new ArrayList<>();
    private final GuiScreen prevScreen;
    private OptionPage currentPage;
    private EasyButton applyButton, closeButton, undoButton;
    private EasyButton donateButton;
    private boolean hasPendingChanges;
    private ControlElement<?> hoveredElement;
    private int buttonId;

    public SodiumOptionsGUI(GuiScreen prevScreen) {
        this.prevScreen = prevScreen;
        buttonId = 0;
        this.pages.add(SodiumGameOptionPages.general());
        this.pages.add(SodiumGameOptionPages.quality());
        this.pages.add(SodiumGameOptionPages.advanced());
        this.pages.add(SodiumGameOptionPages.performance());
    }

    public void setPage(OptionPage page) {
        this.currentPage = page;

        this.rebuildGUI();
    }

    @Override
    public void initGui() {
        super.initGui();

        this.rebuildGUI();
    }

    private void rebuildGUI() {
        this.controls.clear();
        if (this.currentPage == null) {
            if (this.pages.isEmpty()) {
                throw new IllegalStateException("No pages are available?!");
            }

            // Just use the first page for now
            this.currentPage = this.pages.get(0);
        }

        this.rebuildGUIPages();
        this.rebuildGUIOptions();
        this.undoButton = new EasyButton(buttonId++, new Dim2i(this.width - 211, this.height - 26, 65, 20), new TextComponentTranslation("sodium.options.buttons.undo"), this::undoChanges);
        this.applyButton = new EasyButton(buttonId++, new Dim2i(this.width - 142, this.height - 26, 65, 20), new TextComponentTranslation("sodium.options.buttons.apply"), this::applyChanges);
        this.closeButton = new EasyButton(buttonId++, new Dim2i(this.width - 73, this.height - 26, 65, 20), new TextComponentTranslation("gui.done"), this::onGuiClosed);
        TextComponentTranslation donateToJelly = new TextComponentTranslation("sodium.options.buttons.donate");
        int width = 12 + this.fontRenderer.getStringWidth(donateToJelly.getFormattedText());
        this.donateButton = new EasyButton(buttonId++, new Dim2i(this.width - width - 32, 6, width, 20), donateToJelly, this::openDonationPage);
        if (SodiumClientMod.options().notifications.hideDonationButton) {
            this.setDonationButtonVisibility(false);
        }

        buttonList.add(undoButton);
        buttonList.add(applyButton);
        buttonList.add(closeButton);
        buttonList.add(donateButton);

        /*
        for (Element element : this.children) {
            if (element instanceof Drawable) {
                this.drawable.add((Drawable) element);
            }
        }
         */
    }

    private void setDonationButtonVisibility(boolean value) {
        this.donateButton.visible = value;
    }

    private void hideDonationButton() {
        SodiumGameOptions options = SodiumClientMod.options();
        options.notifications.hideDonationButton = true;

        try {
            options.writeChanges();
        } catch (IOException e) {
            throw new RuntimeException("Failed to save configuration", e);
        }

        this.setDonationButtonVisibility(false);
    }

    private void rebuildGUIPages() {
        int x = 6;
        int y = 6;
        /*
        for (OptionPage page : this.pages) {
            int width = 12 + this.fontRenderer.getStringWidth(page.getNewName());

            FlatButtonWidget button = new FlatButtonWidget(new Dim2i(x, y, width, 18), page.getNewName(), () -> this.setPage(page));
            button.setSelected(this.currentPage == page);

            x += width + 6;

            this.children.add(button);
        }
         */
    }

    private void rebuildGUIOptions() {
        int x = 6;
        int y = 28;
        /*
        for (OptionGroup group : this.currentPage.getGroups()) {
            // Add each option's control element
            for (Option<?> option : group.getOptions()) {
                Control<?> control = option.getControl();
                ControlElement<?> element = control.createElement(new Dim2i(x, y, 200, 18));

                this.controls.add(element);
                this.children.add(element);

                // Move down to the next option
                y += 18;
            }

            // Add padding beneath each option group
            y += 4;
        }
         */
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawDefaultBackground();
        updateControls();
        if (this.hoveredElement != null) {

        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void renderToolTip(ItemStack stack, int x, int y) {
        super.renderToolTip(stack, x, y);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);
    }

    private void updateControls() {
        ControlElement<?> hovered = this.getActiveControls()
                .filter(ControlElement::isHovered)
                .findFirst()
                .orElse(null);

        boolean hasChanges = this.getAllOptions()
                .anyMatch(Option::hasChanged);

        for (OptionPage page : this.pages) {
            for (Option<?> option : page.getOptions()) {
                if (option.hasChanged()) {
                    hasChanges = true;
                }
            }
        }

        this.applyButton.enabled = hasChanges;
        this.undoButton.enabled = hasChanges;
        this.closeButton.enabled = hasChanges;

        this.hasPendingChanges = hasChanges;
        this.hoveredElement = hovered;
    }

    private Stream<Option<?>> getAllOptions() {
        return this.pages.stream()
                .flatMap(s -> s.getOptions().stream());
    }

    private Stream<ControlElement<?>> getActiveControls() {
        return this.controls.stream();
    }
    /*
    private void renderOptionTooltip(MatrixStack matrixStack, ControlElement<?> element) {
        Dim2i dim = element.getDimensions();

        int textPadding = 3;
        int boxPadding = 3;

        int boxWidth = 200;

        int boxY = dim.getOriginY();
        int boxX = dim.getLimitX() + boxPadding;

        Option<?> option = element.getOption();
        List<OrderedText> tooltip = new ArrayList<>(this.textRenderer.wrapLines(option.getTooltip(), boxWidth - (textPadding * 2)));

        OptionImpact impact = option.getImpact();

        if (impact != null) {
        	tooltip.add(Language.getInstance().reorder(new TranslatableText("sodium.options.performance_impact_string", impact.toDisplayString()).formatted(Formatting.GRAY)));
        }

        int boxHeight = (tooltip.size() * 12) + boxPadding;
        int boxYLimit = boxY + boxHeight;
        int boxYCutoff = this.height - 40;

        // If the box is going to be cutoff on the Y-axis, move it back up the difference
        if (boxYLimit > boxYCutoff) {
            boxY -= boxYLimit - boxYCutoff;
        }

        this.fillGradient(matrixStack, boxX, boxY, boxX + boxWidth, boxY + boxHeight, 0xE0000000, 0xE0000000);

        for (int i = 0; i < tooltip.size(); i++) {
            this.textRenderer.draw(matrixStack, tooltip.get(i), boxX + textPadding, boxY + textPadding + (i * 12), 0xFFFFFFFF);
        }
    }
     */

    private void applyChanges() {
        final HashSet<OptionStorage<?>> dirtyStorages = new HashSet<>();
        final EnumSet<OptionFlag> flags = EnumSet.noneOf(OptionFlag.class);

        this.getAllOptions().forEach((option -> {
            if (!option.hasChanged()) {
                return;
            }

            option.applyChanges();

            flags.addAll(option.getFlags());
            dirtyStorages.add(option.getStorage());
        }));

        Minecraft client = Minecraft.getMinecraft();

        if (flags.contains(OptionFlag.REQUIRES_RENDERER_RELOAD)) {
            client.renderGlobal.setWorldAndLoadRenderers(client.renderGlobal.world);
        }

        if (flags.contains(OptionFlag.REQUIRES_ASSET_RELOAD)) {
            client.getTextureMapBlocks().setMipmapLevels(client.gameSettings.mipmapLevels);
            client.scheduleResourcesRefresh();
        }

        for (OptionStorage<?> storage : dirtyStorages) {
            storage.save();
        }
    }

    private void undoChanges() {
        this.getAllOptions()
                .forEach(Option::reset);
    }

    private void openDonationPage() {
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_P) {
            Minecraft.getMinecraft().displayGuiScreen(new GuiVideoSettings(this.prevScreen, Minecraft.getMinecraft().gameSettings));
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void onGuiClosed() {
        this.mc.displayGuiScreen(this.prevScreen);
    }

}
