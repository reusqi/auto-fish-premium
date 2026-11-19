package com.adith.autofish.client.gui;

import com.adith.autofish.AutoFishManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
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

        addToggleButton(x, y, buttonWidth, "Auto-Fish", AutoFishManager.isEnabled(), AutoFishManager::setEnabled);
        addToggleButton(x, y + 28, buttonWidth, "Durability Protection", AutoFishManager.isDurabilityProtectionEnabled(), AutoFishManager::setDurabilityProtectionEnabled);
        addToggleButton(x, y + 56, buttonWidth, "Auto-Eat", AutoFishManager.isAutoEatEnabled(), AutoFishManager::setAutoEatEnabled);
        addToggleButton(x, y + 84, buttonWidth, "HUD Notifications", AutoFishManager.isNotificationsEnabled(), AutoFishManager::setNotificationsEnabled);
        addToggleButton(x, y + 112, buttonWidth, "Random Recast Delay", AutoFishManager.isRandomRecastDelayEnabled(), AutoFishManager::setRandomRecastDelayEnabled);

        this.addRenderableWidget(Button.builder(Component.literal("Done"), button -> this.onClose())
                .bounds(x, y + 152, buttonWidth, 24)
                .build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderTransparentBackground(graphics);

        int panelWidth = Math.min(400, this.width - 24);
        int panelHeight = 260;
        int left = (this.width - panelWidth) / 2;
        int top = Math.max(20, (this.height - panelHeight) / 2);
        int right = left + panelWidth;
        int bottom = top + panelHeight;

        graphics.fill(left, top, right, bottom, 0xDD101820);
        graphics.fill(left, top, right, top + 3, 0xFF3DDC97);
        graphics.fill(left, top + 3, right, top + 4, 0xFF4CA3FF);

        graphics.drawCenteredString(this.font, Component.literal("Auto Fish Premium").withStyle(ChatFormatting.AQUA), this.width / 2, top + 14, 0xFFFFFFFF);
        graphics.drawCenteredString(this.font, Component.literal("H toggles fishing  |  J opens this panel").withStyle(ChatFormatting.GRAY), this.width / 2, top + 31, 0xFFB8C7D9);
        graphics.drawString(this.font, Component.literal("Rod shutoff at " + AutoFishManager.getLowDurabilityLimit() + " durability").withStyle(ChatFormatting.YELLOW), left + 22, bottom - 42, 0xFFFFE08A);
        graphics.drawString(this.font, Component.literal("Auto-eat starts at " + AutoFishManager.getEatHungerThreshold() + " hunger or lower").withStyle(ChatFormatting.YELLOW), left + 22, bottom - 28, 0xFFFFE08A);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(parent);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void addToggleButton(int x, int y, int width, String label, boolean initialValue, ToggleSetter setter) {
        this.addRenderableWidget(Button.builder(toggleLabel(label, initialValue), button -> {
            boolean next = !currentValue(label);
            setter.set(next);
            button.setMessage(toggleLabel(label, next));
        }).bounds(x, y, width, 22).build());
    }

    private boolean currentValue(String label) {
        return switch (label) {
            case "Auto-Fish" -> AutoFishManager.isEnabled();
            case "Durability Protection" -> AutoFishManager.isDurabilityProtectionEnabled();
            case "Auto-Eat" -> AutoFishManager.isAutoEatEnabled();
            case "HUD Notifications" -> AutoFishManager.isNotificationsEnabled();
            case "Random Recast Delay" -> AutoFishManager.isRandomRecastDelayEnabled();
            default -> false;
        };
    }

    private Component toggleLabel(String label, boolean value) {
        return Component.literal(label + ": " + (value ? "ON" : "OFF"))
                .withStyle(value ? ChatFormatting.GREEN : ChatFormatting.RED);
    }

    @FunctionalInterface
    private interface ToggleSetter {
        void set(boolean value);
    }
}
