package com.adith.autofish.client;

import com.adith.autofish.AutoFishManager;
import com.adith.autofish.client.gui.AutoFishConfigScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class AutoFishPremiumClient implements ClientModInitializer {
	private static KeyMapping toggleKey;
	private static KeyMapping configKey;

	@Override
	public void onInitializeClient() {
		toggleKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.autofish.toggle",
				GLFW.GLFW_KEY_H,
				KeyMapping.Category.GAMEPLAY
		));
		configKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.autofish.config",
				GLFW.GLFW_KEY_J,
				KeyMapping.Category.GAMEPLAY
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (toggleKey.consumeClick()) {
				AutoFishManager.toggleEnabled();
			}

			while (configKey.consumeClick()) {
				client.setScreen(new AutoFishConfigScreen(client.screen));
			}
		});
	}
}
