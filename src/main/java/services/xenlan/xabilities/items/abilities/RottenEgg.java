package services.xenlan.xabilities.items.abilities;

import org.bukkit.entity.Egg;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import services.xenlan.xabilities.cooldown.Cooldowns;
import services.xenlan.xabilities.items.AbilityEvents;
import services.xenlan.xabilities.util.chatUtil;
import services.xenlan.xabilities.xAbilities;

import java.util.*;

public class RottenEgg implements Listener {

    private AbilityEvents item = AbilityEvents.ROTTENEGG;

    @EventHandler
    public void clickCooldowns(PlayerInteractEvent event) {
        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            if (!item.isSimilar(event.getPlayer().getItemInHand()))
                return;
            if (Cooldowns.getRottenegg().onCooldown(event.getPlayer())) {
                for (String list : xAbilities.config.getConfig().getStringList("Message.Item.OnCooldown")) {
                    event.getPlayer().sendMessage(chatUtil.chat(list.replace("%ability%", xAbilities.config.getConfig().getString
                            ("RottenEgg.Name")).replace("%time%", Cooldowns.getRottenegg().getRemaining(event.getPlayer()))));
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler

    public void onClick(PlayerInteractEvent event) {

        Player p = event.getPlayer();

        if (!event.getAction().name().contains("RIGHT"))
            return;

        if (!item.isSimilar(p.getItemInHand()))
            return;

        if (Cooldowns.getRefundcool().onCooldown(p)) {
            for (String list : xAbilities.config.getConfig().getStringList("Refund.Message.Cooldown")) {
                p.sendMessage(chatUtil.chat(list.replace("%time%", Cooldowns.getRefundcool().getRemaining(p))));
                event.setUseItemInHand(Event.Result.DENY);
                event.setCancelled(true);
                p.updateInventory();
                return;
            }
        }

        if (Cooldowns.getAbilitycd().onCooldown(p)) {
            for (String list : xAbilities.config.getConfig().getStringList("Message.Ability.OnCooldown")) {
                p.sendMessage(chatUtil.chat(list.replace("%time%", Cooldowns.getAbilitycd().getRemaining(p))));
                event.setCancelled(true);
                event.setUseItemInHand(Event.Result.DENY);
                p.updateInventory();
                return;
            }
        }
        if (Cooldowns.getRottenegg().onCooldown(p)) {
            for (String list : xAbilities.config.getConfig().getStringList("Message.Item.OnCooldown")) {
                p.sendMessage(chatUtil.chat(list.replace("%ability%", xAbilities.config.getConfig().getString("RottenEgg.Name")).replace("%time%", Cooldowns.getRottenegg().getRemaining(p))));
                event.setCancelled(true);
                event.setUseItemInHand(Event.Result.DENY);
                p.updateInventory();
                return;
            }
        }
        if (xAbilities.config.getConfig().getBoolean("Ability-Cooldown-Enabled")) {
            Cooldowns.getAbilitycd().applyCooldown(p, xAbilities.config.getConfig().getInt("Ability-Cooldown") * 1000);
        }
        Egg snowball = event.getPlayer().launchProjectile(Egg.class);
        Cooldowns.getRefundcool().removeCooldown(p);
        snowball.setMetadata("rottenegg", new FixedMetadataValue(xAbilities.getInstance(), p.getUniqueId()));
        Cooldowns.getRottenegg().applyCooldown(p, xAbilities.config.getConfig().getInt("RottenEgg.Cooldown") * 1000);
        AbilityEvents.takeItem(p, p.getItemInHand());
        xAbilities.getEggRefunds().put(p, snowball);
        event.setCancelled(true);
    }



    Set<ItemStack> items = new HashSet<>();
    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {

        if (event.getEntity() instanceof Player) {
            if (event.getDamager() instanceof Egg) {
                Projectile snowball = (Projectile) event.getDamager();
                Player damager = (Player) snowball.getShooter();
                Player damaged = (Player) event.getEntity();

                if (!item.isSimilar(damager.getItemInHand()))
                    return;

                if (!snowball.hasMetadata("rottenegg"))
                    return;
                if (item.isSimilar(damaged.getItemInHand())) {
                    if (AbilityEvents.checkLocation(damaged.getLocation(), xAbilities.config.getConfig().getInt("SPAWN.Radius"), 0, 0)) {
                        for (String list : xAbilities.config.getConfig().getStringList("SPAWN.CantUse")) {
                            damaged.sendMessage(chatUtil.chat(list));
                        }
                        event.setCancelled(true);
                        return;
                    }
                }

                if (item.isSimilar(damager.getItemInHand())) {
                    if (AbilityEvents.checkLocation(damager.getLocation(), xAbilities.config.getConfig().getInt("SPAWN.Radius"), 0, 0)) {
                        for (String list : xAbilities.config.getConfig().getStringList("SPAWN.CantUse")) {
                            damager.sendMessage(chatUtil.chat(list));
                        }
                        event.setCancelled(true);
                        return;
                    }
                }

                if (xAbilities.getEggRefunds().containsKey(damager)) {
                    for (String section : xAbilities.config.getConfig().getConfigurationSection("RottenEgg.Effects").getKeys(false)) {

                        int power = xAbilities.config.getConfig().getInt("RottenEgg.Effects." + section + ".Power");
                        int time = xAbilities.config.getConfig().getInt("RottenEgg.Effects." + section + ".Time");
                        String type = xAbilities.config.getConfig().getString("RottenEgg.Effects." + section + ".Type");
                        damaged.addPotionEffect(new PotionEffect(PotionEffectType.getByName(type), time * 20, power - 1));
                    }
                    for (String list : xAbilities.config.getConfig().getStringList("RottenEgg.Message.Hit-Someone")) {
                        damager.sendMessage(chatUtil.chat(list).replaceAll("%damaged%", damaged.getName()));
                    }
                    for (String list : xAbilities.config.getConfig().getStringList("RottenEgg.Message.Been-Hit")) {
                        damaged.sendMessage(chatUtil.chat(list).replaceAll("%damager%", damaged.getName()));
                    }
                    xAbilities.getEggRefunds().remove(damager);
                }
            }
        }
    }

}