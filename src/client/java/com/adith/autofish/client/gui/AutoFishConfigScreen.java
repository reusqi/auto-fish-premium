package com.adith.autofish.client.gui;

import com.adith.autofish.AutoFishManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class AutoFishConfigScreen extends Screen {
    private final Screen parent;

    public AutoFishConfigScreen(Screen parent) {
        super(Component.literal("Auto Fish Premium"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int panelWidth = Math.min(360, this.width - 32);
        int left = (this.width - panelWidth) / 2;
        int buttonWidth = panelWidth - 40;
        int x = left + 20;
        int y = Math.max(58, this.height / 2 - 96);

        addRenderableWidget(Button.builder(toggleLabel("Auto-Fish", AutoFishManager.isEnabled()), button -> {
            boolean next = !AutoFishManager.isEnabled();
            AutoFishManager.setEnabled(next);
            button.setMessage(toggleLabel("Auto-Fish", next));
        }).bounds(x, y, buttonWidth, 22).build());
        addRenderableWidget(Button.builder(toggleLabel("Durability Protection", AutoFishManager.isDurabilityProtectionEnabled()), button -> {
            boolean next = !AutoFishManager.isDurabilityProtectionEnabled();
            AutoFishManager.setDurabilityProtectionEnabled(next);
            button.setMessage(toggleLabel("Durability Protection", next));
        }).bounds(x, y + 28, buttonWidth, 22).build());
        addRenderableWidget(Button.builder(toggleLabel("Auto-Eat", AutoFishManager.isAutoEatEnabled()), button -> {
            boolean next = !AutoFishManager.isAutoEatEnabled();
            AutoFishManager.setAutoEatEnabled(next);
            button.setMessage(toggleLabel("Auto-Eat", next));
        }).bounds(x, y + 56, buttonWidth, 22).build());
        addRenderableWidget(Button.builder(toggleLabel("HUD Notifications", AutoFishManager.isNotificationsEnabled()), button -> {
            boolean next = !AutoFishManager.isNotificationsEnabled();
            AutoFishManager.setNotificationsEnabled(next);
            button.setMessage(toggleLabel("HUD Notifications", next));
        }).bounds(x, y + 84, buttonWidth, 22).build());
        addRenderableWidget(Button.builder(toggleLabel("Random Recast Delay", AutoFishManager.isRandomRecastDelayEnabled()), button -> {
            boolean next = !AutoFishManager.isRandomRecastDelayEnabled();
            AutoFishManager.setRandomRecastDelayEnabled(next);
            button.setMessage(toggleLabel("Random Recast Delay", next));
        }).bounds(x, y + 112, buttonWidth, 22).build());

        this.addRenderableWidget(Button.builder(Component.literal("Done"), button -> this.onClose())
                .bounds(x, y + 152, buttonWidth, 24)
                .build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int panelWidth = Math.min(400, this.width - 24);
        int panelHeight = 260;
        int left = (this.width - panelWidth) / 2;
        int top = Math.max(20, (this.height - panelHeight) / 2);
        int right = left + panelWidth;
        int bottom = top + panelHeight;

        graphics.fill(left, top, right, bottom, 0xDD101820);
        graphics.fill(left, top, right, top + 3, 0xFF3DDC97);
        graphics.fill(left, top + 3, right, top + 4, 0xFF4CA3FF);

        graphics.centeredText(this.font, Component.literal("Auto Fish Premium").withStyle(ChatFormatting.AQUA), this.width / 2, top + 14, 0xFFFFFFFF);
        graphics.centeredText(this.font, Component.literal("H toggles fishing  |  J opens this panel").withStyle(ChatFormatting.GRAY), this.width / 2, top + 31, 0xFFB8C7D9);
        graphics.text(this.font, Component.literal("Rod shutoff at " + AutoFishManager.getLowDurabilityLimit() + " durability").withStyle(ChatFormatting.YELLOW), left + 22, bottom - 42, 0xFFFFE08A);
        graphics.text(this.font, Component.literal("Auto-eat starts at " + AutoFishManager.getEatHungerThreshold() + " hunger or lower").withStyle(ChatFormatting.YELLOW), left + 22, bottom - 28, 0xFFFFE08A);

        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.gui.setScreen(parent);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private Component toggleLabel(String label, boolean value) {
        return Component.literal(label + ": " + (value ? "ON" : "OFF"))
                .withStyle(value ? ChatFormatting.GREEN : ChatFormatting.RED);
    }
}
