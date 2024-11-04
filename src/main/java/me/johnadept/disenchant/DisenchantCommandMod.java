package me.johnadept.disenchant;

import me.johnadept.disenchant.commands.DisenchantCommand;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DisenchantCommandMod implements ModInitializer {
	public static final String MOD_ID = "disenchant-command";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			DisenchantCommand.register(dispatcher, registryAccess);
		});
	}
}