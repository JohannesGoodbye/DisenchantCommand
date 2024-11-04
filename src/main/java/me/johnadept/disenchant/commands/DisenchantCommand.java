package me.johnadept.disenchant.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.RegistryEntryReferenceArgumentType;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.Collection;
import java.util.Iterator;

public class DisenchantCommand {
    private static final DynamicCommandExceptionType FAILED_ENTITY_EXCEPTION = new DynamicCommandExceptionType((entityName) -> {
        return Text.literal(entityName + " is not a valid entity for this command");
    });
    private static final DynamicCommandExceptionType FAILED_ITEMLESS_EXCEPTION = new DynamicCommandExceptionType((entityName) -> {
        return Text.literal(entityName + " is not holding any item");
    });
    private static final DynamicCommandExceptionType FAILED_INCOMPATIBLE_EXCEPTION = new DynamicCommandExceptionType((itemName) -> {
        return Text.literal(itemName + " does not have that enchantment");
    });

    private static final SimpleCommandExceptionType FAILED_EXCEPTION = new SimpleCommandExceptionType(Text.literal("Nothing changed. Targets either have no item in their hands or the enchantment could not be removed"));
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder) CommandManager.literal("disenchant").requires((source) -> {
            return source.hasPermissionLevel(2);
        })).then(CommandManager.argument("targets", EntityArgumentType.entities()).then(((RequiredArgumentBuilder)CommandManager.argument("enchantment", RegistryEntryReferenceArgumentType.registryEntry(registryAccess, RegistryKeys.ENCHANTMENT)).executes((context) -> {
            return execute((ServerCommandSource)context.getSource(), EntityArgumentType.getEntities(context, "targets"), RegistryEntryReferenceArgumentType.getEnchantment(context, "enchantment"));
        })))));
    }

    private static int execute(ServerCommandSource source, Collection<? extends Entity> targets, RegistryEntry<Enchantment> enchantment) throws CommandSyntaxException {
        int i = 0;
        Iterator var6 = targets.iterator();

        while(true) {
            while(true) {
                while(true) {
                    while(var6.hasNext()) {
                        Entity entity = (Entity)var6.next();
                        if (entity instanceof LivingEntity) {
                            LivingEntity livingEntity = (LivingEntity)entity;
                            ItemStack itemStack = livingEntity.getMainHandStack();
                            if (!itemStack.isEmpty()) {
                                ItemEnchantmentsComponent enchantmentsComponent = EnchantmentHelper.getEnchantments(itemStack);
                                if (enchantmentsComponent.getEnchantments().contains(enchantment)) {
                                    DisenchantCommand.grind(itemStack, enchantment);
                                    ++i;
                                } else if (targets.size() == 1) {
                                    throw FAILED_INCOMPATIBLE_EXCEPTION.create(itemStack.getName().getString());
                                }
                            } else if (targets.size() == 1) {
                                throw FAILED_ITEMLESS_EXCEPTION.create(livingEntity.getName().getString());
                            }
                        } else if (targets.size() == 1) {
                            throw FAILED_ENTITY_EXCEPTION.create(entity.getName().getString());
                        }
                    }

                    if (i == 0) {
                        throw FAILED_EXCEPTION.create();
                    }
                    if (targets.size() == 1) {
                        source.sendFeedback(() -> Text.literal("Removed enchantment " + enchantment.getIdAsString() + " from " + (targets.iterator().next()).getNameForScoreboard()), true);
                    } else {
                        source.sendFeedback(() -> Text.literal("Removed enchantment " + enchantment.getIdAsString() + " from " + targets.size() + " entities"), true);
                    }
                    return i;
                }
            }
        }
    }

    private static ItemStack grind(ItemStack item, RegistryEntry<Enchantment> entry) {
        ItemEnchantmentsComponent itemEnchantmentsComponent = EnchantmentHelper.apply(item, (components) -> {
            components.remove((enchantment) -> {
                return enchantment.equals(entry);
            });
        });
        if (item.isOf(Items.ENCHANTED_BOOK) && itemEnchantmentsComponent.isEmpty()) {
            item = item.withItem(Items.BOOK);
        }

        int i = 0;

        for(int j = 0; j < itemEnchantmentsComponent.getSize(); ++j) {
            i = AnvilScreenHandler.getNextCost(i);
        }

        return item;
    }
}
