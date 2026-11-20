package com.adith.autofish;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class AutoFishManager {
    private static final int LOW_DURABILITY_LIMIT = 5;
    private static final int EAT_HUNGER_THRESHOLD = 14;

    private static boolean enabled;
    private static boolean durabilityProtectionEnabled = true;
    private static boolean autoEatEnabled = true;
    private static boolean notificationsEnabled = true;
    private static boolean randomRecastDelayEnabled = true;
    private static boolean eating;
    private static int savedRodSlot = -1;
    private static int foodSlot = -1;
    private static int eatingTicks;
    private static int eatingStartFoodLevel;
    private static int noFoodMessageCooldown;
    private static long lastSplashTime;
    private static int recastDelayTicks;
    private static net.minecraft.client.multiplayer.ClientLevel recastLevel;

    public static void handleSplashSound(double soundX, double soundY, double soundZ) {
        if (!enabled || eating) return;
        long now = System.currentTimeMillis();
        if (now - lastSplashTime < 500) return;
        lastSplashTime = now;

        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (player == null || player.fishing == null) return;
        if (!player.getMainHandItem().is(Items.FISHING_ROD)) return;

        double deltaX = player.fishing.getX() - soundX;
        double deltaY = player.fishing.getY() - soundY;
        double deltaZ = player.fishing.getZ() - soundZ;
        double distanceSquared = (deltaX * deltaX) + (deltaY * deltaY) + (deltaZ * deltaZ);

        if (distanceSquared <= 2.25) {
            executeAutoFish(client, player);
        }
    }

    private static void executeAutoFish(Minecraft client, LocalPlayer player) {
        if (client.gameMode == null) return;
        if (!canUseRod(client, player, "Auto-Fish disabled: rod durability is too low", true)) return;

        client.gameMode.useItem(player, InteractionHand.MAIN_HAND);

        long randomDelay = randomRecastDelayEnabled ? 900 + (long) (Math.random() * 500) : 900;
        recastDelayTicks = (int) (randomDelay / 50);
        recastLevel = client.level;
    }

    public static void onPlayerTick(LocalPlayer player) {
        if (noFoodMessageCooldown > 0) {
            noFoodMessageCooldown--;
        }

        Minecraft client = Minecraft.getInstance();
        if (client.gui.screen() != null) {
            finishEating(client, player);
            return;
        }

        if (recastDelayTicks > 0) {
            recastDelayTicks--;
            if (recastDelayTicks == 0 && enabled && !eating && client.player == player && client.level == recastLevel && client.gameMode != null && player.fishing == null && player.getMainHandItem().is(Items.FISHING_ROD)) {
                if (canUseRod(client, player, "Auto-Fish disabled: rod durability is too low", true)) {
                    client.gameMode.useItem(player, InteractionHand.MAIN_HAND);
                }
            }
        }

        if (!enabled || !autoEatEnabled || player == null) {
            finishEating(client, player);
            return;
        }

        if (client.gameMode == null) {
            finishEating(client, player);
            return;
        }

        if (eating) {
            eatingTicks++;
            client.options.keyUse.setDown(true);

            if (!player.getInventory().getSelectedItem().equals(player.getMainHandItem())) {
                setSelectedSlot(client, player, player.getInventory().getSelectedSlot());
            }

            if (!player.isUsingItem() && eatingTicks % 5 == 0 && shouldEat(player)) {
                client.gameMode.useItem(player, InteractionHand.MAIN_HAND);
            }

            if (!shouldEat(player) || player.getFoodData().getFoodLevel() > eatingStartFoodLevel || eatingTicks > 80) {
                finishEating(client, player);
                recastAfterEating(client);
            }
            return;
        }

        if (player.fishing != null && shouldEat(player)) {
            int slot = findFoodSlot(player);
            if (slot >= 0) {
                savedRodSlot = player.getInventory().getSelectedSlot();
                foodSlot = slot;
                setSelectedSlot(client, player, slot);
                eating = true;
                eatingTicks = 0;
                eatingStartFoodLevel = player.getFoodData().getFoodLevel();
                client.options.keyUse.setDown(true);
                client.gameMode.useItem(player, InteractionHand.MAIN_HAND);
                showActionBar(Component.literal("Auto-Eat: eating food").withStyle(ChatFormatting.YELLOW), false);
            } else if (noFoodMessageCooldown == 0) {
                showActionBar(Component.literal("Auto-Eat: no food in hotbar").withStyle(ChatFormatting.RED), false);
                noFoodMessageCooldown = 100;
            }
        }
    }

    public static void toggleEnabled() {
        setEnabled(!enabled);
    }

    public static void setEnabled(boolean value) {
        enabled = value;
        if (!enabled) {
            recastDelayTicks = 0;
            finishEating(Minecraft.getInstance(), Minecraft.getInstance().player);
        }
        showActionBar(Component.literal(enabled ? "Auto-Fish Enabled" : "Auto-Fish Disabled")
                .withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.RED), false);
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static boolean isDurabilityProtectionEnabled() {
        return durabilityProtectionEnabled;
    }

    public static void setDurabilityProtectionEnabled(boolean value) {
        durabilityProtectionEnabled = value;
        showToggle("Durability Protection", value);
    }

    public static boolean isAutoEatEnabled() {
        return autoEatEnabled;
    }

    public static void setAutoEatEnabled(boolean value) {
        autoEatEnabled = value;
        if (!value) {
            finishEating(Minecraft.getInstance(), Minecraft.getInstance().player);
        }
        showToggle("Auto-Eat", value);
    }

    public static boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    public static void setNotificationsEnabled(boolean value) {
        notificationsEnabled = value;
        showToggle("HUD Notifications", value);
    }

    public static boolean isRandomRecastDelayEnabled() {
        return randomRecastDelayEnabled;
    }

    public static void setRandomRecastDelayEnabled(boolean value) {
        randomRecastDelayEnabled = value;
        showToggle("Random Recast Delay", value);
    }

    public static int getLowDurabilityLimit() {
        return LOW_DURABILITY_LIMIT;
    }

    public static int getEatHungerThreshold() {
        return EAT_HUNGER_THRESHOLD;
    }

    private static boolean canUseRod(Minecraft client, LocalPlayer player, String warning, boolean force) {
        if (!durabilityProtectionEnabled) return true;

        ItemStack stack = player.getMainHandItem();
        if (!stack.is(Items.FISHING_ROD)) return false;

        int remaining = getRemainingDurability(stack);
        if (remaining >= 0 && remaining <= LOW_DURABILITY_LIMIT) {
            enabled = false;
            recastDelayTicks = 0;
            finishEating(client, player);
            showActionBar(Component.literal(warning).withStyle(ChatFormatting.RED), true);
            return false;
        }

        return true;
    }

    private static int getRemainingDurability(ItemStack stack) {
        if (!stack.isDamageableItem()) return -1;
        return stack.getMaxDamage() - stack.getDamageValue();
    }

    private static boolean shouldEat(LocalPlayer player) {
        return player.getFoodData().getFoodLevel() <= EAT_HUNGER_THRESHOLD && player.canEat(false);
    }

    private static int findFoodSlot(LocalPlayer player) {
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            FoodProperties food = stack.get(DataComponents.FOOD);
            if (food != null && (player.canEat(food.canAlwaysEat()) || player.canEat(false))) {
                return slot;
            }
        }
        return -1;
    }

    private static void finishEating(Minecraft client, LocalPlayer player) {
        if (!eating) return;

        client.options.keyUse.setDown(false);
        if (player != null && savedRodSlot >= 0 && savedRodSlot < 9) {
            if (player.getInventory().getSelectedSlot() == foodSlot) {
                setSelectedSlot(client, player, savedRodSlot);
            }
        }

        eating = false;
        savedRodSlot = -1;
        foodSlot = -1;
        eatingTicks = 0;
        eatingStartFoodLevel = 0;
    }

    private static void recastAfterEating(Minecraft client) {
        if (!enabled || client.player == null || client.gameMode == null) return;
        if (!client.player.getMainHandItem().is(Items.FISHING_ROD)) return;
        if (!canUseRod(client, client.player, "Auto-Fish disabled: rod durability is too low", true)) return;

        recastDelayTicks = 6;
        recastLevel = client.level;
    }

    private static void setSelectedSlot(Minecraft client, LocalPlayer player, int slot) {
        player.getInventory().setSelectedSlot(slot);
        if (client.getConnection() != null) {
            client.getConnection().send(new ServerboundSetCarriedItemPacket(slot));
        }
    }

    private static void showToggle(String name, boolean value) {
        showActionBar(Component.literal(name + (value ? " Enabled" : " Disabled"))
                .withStyle(value ? ChatFormatting.GREEN : ChatFormatting.RED), false);
    }

    private static void showActionBar(Component message, boolean force) {
        Minecraft client = Minecraft.getInstance();
        if (client.gui == null || client.player == null) return;
        if (!force && !notificationsEnabled) return;
        client.player.sendOverlayMessage(message);
    }

    private static void showActionBar(Component message) {
        showActionBar(message, false);
    }
}