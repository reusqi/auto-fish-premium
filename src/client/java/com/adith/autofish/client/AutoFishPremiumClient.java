package com.adith.autofish.client;

import com.adith.autofish.AutoFishManager;
import com.adith.autofish.client.gui.AutoFishConfigScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

public class AutoFishPremiumClient implements ClientModInitializer {
	private static KeyMapping toggleKey;
	private static KeyMapping configKey;

	@Override
	public void onInitializeClient() {
		toggleKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.autofish.toggle",
				InputConstants.Type.KEYBOARD,
				InputConstants.KEY_H,
				KeyMapping.Category.GAMEPLAY
		));
		configKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.autofish.config",
				InputConstants.Type.KEYBOARD,
				InputConstants.KEY_J,
				KeyMapping.Category.GAMEPLAY
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (toggleKey.consumeClick()) {
				AutoFishManager.toggleEnabled();
			}

			while (configKey.consumeClick()) {
				client.gui.setScreen(new AutoFishConfigScreen(client.gui.screen()));
			}
		});
	}
}
