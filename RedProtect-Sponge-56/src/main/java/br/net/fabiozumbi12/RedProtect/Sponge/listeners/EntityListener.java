/*
 * Copyright (c) 2020 - @FabioZumbi12
 * Last Modified: 02/07/2020 19:01.
 *
 * This class is provided 'as-is', without any express or implied warranty. In no event will the authors be held liable for any
 *  damages arising from the use of this class.
 *
 * Permission is granted to anyone to use this class for any purpose, including commercial plugins, and to alter it and
 * redistribute it freely, subject to the following restrictions:
 * 1 - The origin of this class must not be misrepresented; you must not claim that you wrote the original software. If you
 * use this class in other plugins, an acknowledgment in the plugin documentation would be appreciated but is not required.
 * 2 - Altered source versions must be plainly marked as such, and must not be misrepresented as being the original class.
 * 3 - This notice may not be removed or altered from any source distribution.
 *
 * Esta classe é fornecida "como está", sem qualquer garantia expressa ou implícita. Em nenhum caso os autores serão
 * responsabilizados por quaisquer danos decorrentes do uso desta classe.
 *
 * É concedida permissão a qualquer pessoa para usar esta classe para qualquer finalidade, incluindo plugins pagos, e para
 * alterá-lo e redistribuí-lo livremente, sujeito às seguintes restrições:
 * 1 - A origem desta classe não deve ser deturpada; você não deve afirmar que escreveu a classe original. Se você usar esta
 *  classe em um plugin, uma confirmação de autoria na documentação do plugin será apreciada, mas não é necessária.
 * 2 - Versões de origem alteradas devem ser claramente marcadas como tal e não devem ser deturpadas como sendo a
 * classe original.
 * 3 - Este aviso não pode ser removido ou alterado de qualquer distribuição de origem.
 */

package br.net.fabiozumbi12.RedProtect.Sponge.listeners;

import br.net.fabiozumbi12.RedProtect.Core.helpers.LogLevel;
import br.net.fabiozumbi12.RedProtect.Sponge.RedProtect;
import br.net.fabiozumbi12.RedProtect.Sponge.Region;
import br.net.fabiozumbi12.RedProtect.Sponge.helpers.ContainerManager;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.effect.potion.PotionEffectTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.explosive.Explosive;
import org.spongepowered.api.entity.hanging.Hanging;
import org.spongepowered.api.entity.living.*;
import org.spongepowered.api.entity.living.animal.Animal;
import org.spongepowered.api.entity.living.golem.Golem;
import org.spongepowered.api.entity.living.monster.Monster;
import org.spongepowered.api.entity.living.monster.Wither;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.entity.projectile.ThrownPotion;
import org.spongepowered.api.entity.projectile.explosive.fireball.Fireball;
import org.spongepowered.api.entity.projectile.source.ProjectileSource;
import org.spongepowered.api.entity.weather.Lightning;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.entity.spawn.SpawnType;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.entity.projectile.LaunchProjectileEvent;
import org.spongepowered.api.event.filter.IsCancelled;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.world.ExplosionEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.explosion.Explosion;

import java.util.List;
import java.util.Optional;

public class EntityListener {

    private static final ContainerManager cont = new ContainerManager();

    public EntityListener() {
        RedProtect.get().logger.debug(LogLevel.ENTITY, "Loaded EntityListener...");
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    @IsCancelled(Tristate.FALSE)
    public void onCreatureSpawn(SpawnEntityEvent event) {
        for (Entity e : event.getEntities()) {
            if (e == null) {
                continue;
            }

            if (e instanceof ArmorStand && RedProtect.get().getConfigManager().configRoot().hooks.armor_stand_arms) {
                ArmorStand as = (ArmorStand) e;
                as.offer(Keys.ARMOR_STAND_HAS_ARMS, true);
            }

            if (!(e instanceof Living)) {
                continue;
            }

            Optional<SpawnType> cause = event.getCause().first(SpawnType.class);
            RedProtect.get().logger.debug(LogLevel.ENTITY, "SpawnCause: " + (cause.map(Object::toString).orElse(" null")));
            if (e instanceof Wither && cause.isPresent() && cause.get().equals(SpawnTypes.PLACEMENT)) {
                Region r = RedProtect.get().getRegionManager().getTopRegion(e.getLocation(), this.getClass().getName());
                if (r != null && !r.canSpawnWhiter()) {
                    event.setCancelled(true);
                    return;
                }
            }

            if (e instanceof Monster) {
                Location<World> l = e.getLocation();
                Region r = RedProtect.get().getRegionManager().getTopRegion(l, this.getClass().getName());
                if (r != null && !r.canSpawnMonsters()) {
                    RedProtect.get().logger.debug(LogLevel.ENTITY, "Cancelled spawn of monster " + e.getType().getName());
                    event.setCancelled(true);
                    return;
                }
            }
            if (e instanceof Animal || e instanceof Golem || e instanceof Ambient || e instanceof Aquatic) {
                Location<World> l = e.getLocation();
                Region r = RedProtect.get().getRegionManager().getTopRegion(l, this.getClass().getName());
                if (r != null && !r.canSpawnPassives()) {
                    RedProtect.get().logger.debug(LogLevel.ENTITY, "Cancelled spawn of animal " + e.getType().getName());
                    event.setCancelled(true);
                    return;
                }
            }
            RedProtect.get().logger.debug(LogLevel.ENTITY, "EntityListener - Spawn mob " + e.getType().getName());
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onEntityDamage(DamageEntityEvent e) {

        //victim
        Entity e1 = e.getTargetEntity();
        RedProtect.get().logger.debug(LogLevel.ENTITY, "EntityListener - DamageEntityEvent entity target " + e1.getType().getName());
        Region r = RedProtect.get().getRegionManager().getTopRegion(e1.getLocation(), this.getClass().getName());
        if (e1 instanceof Living && !(e1 instanceof Monster)) {
            if (r != null && r.flagExists("invincible")) {
                if (r.getFlagBool("invincible")) {
                    e.setCancelled(true);
                }
            }
        }

        if (e1 instanceof Animal || e1 instanceof Villager || e1 instanceof Golem || e1 instanceof Ambient) {
            if (r != null && r.flagExists("invincible")) {
                if (r.getFlagBool("invincible")) {
                    if (e1 instanceof Animal) {
                        ((Animal) e1).setTarget(null);
                    }
                    e.setCancelled(true);
                }
            }
        }

        //damager
        if (!e.getCause().first(Living.class).isPresent()) {
            return;
        }
        Entity e2 = e.getCause().first(Living.class).get();
        RedProtect.get().logger.debug(LogLevel.ENTITY, "EntityListener - DamageEntityEvent damager " + e2.getType().getName());

        if (e2 instanceof Projectile) {
            Projectile a = (Projectile) e2;
            if (a.getShooter() instanceof Entity) {
                e2 = (Entity) a.getShooter();
            }
        }

        Region r1 = RedProtect.get().getRegionManager().getTopRegion(e1.getLocation(), this.getClass().getName());
        Region r2 = RedProtect.get().getRegionManager().getTopRegion(e2.getLocation(), this.getClass().getName());

        if (e.getCause().containsType(Lightning.class) ||
                e.getCause().containsType(Explosive.class) ||
                e.getCause().containsType(Fireball.class) ||
                e.getCause().containsType(Explosion.class)) {
            if (r1 != null && !r1.canFire() && !(e2 instanceof Player)) {
                e.setCancelled(true);
                return;
            }
        }

        if (e1 instanceof Player) {
            if (e2 instanceof Player) {
                Player p2 = (Player) e2;
                if (r1 != null) {

                    ItemType itemInHand = RedProtect.get().getVersionHelper().getItemInHand(p2);

                    if (itemInHand.getType().equals(ItemTypes.EGG) && !r1.canProtectiles(p2)) {
                        e.setCancelled(true);
                        RedProtect.get().getLanguageManager().sendMessage(p2, "playerlistener.region.cantuse");
                        return;
                    }
                    if (r2 != null) {
                        if (itemInHand.getType().equals(ItemTypes.EGG) && !r2.canProtectiles(p2)) {
                            e.setCancelled(true);
                            RedProtect.get().getLanguageManager().sendMessage(p2, "playerlistener.region.cantuse");
                            return;
                        }
                        if ((r1.flagExists("pvp") && !r1.canPVP((Player) e1, p2)) || (r1.flagExists("pvp") && !r2.canPVP((Player) e1, p2))) {
                            e.setCancelled(true);
                            RedProtect.get().getLanguageManager().sendMessage(p2, "entitylistener.region.cantpvp");
                        }
                    } else if (r1.flagExists("pvp") && !r1.canPVP((Player) e1, p2)) {
                        e.setCancelled(true);
                        RedProtect.get().getLanguageManager().sendMessage(p2, "entitylistener.region.cantpvp");
                    }
                } else if (r2 != null && r2.flagExists("pvp") && !r2.canPVP((Player) e1, p2)) {
                    e.setCancelled(true);
                    RedProtect.get().getLanguageManager().sendMessage(p2, "entitylistener.region.cantpvp");
                }
            }
        } else if (e1 instanceof Animal || e1 instanceof Villager || e1 instanceof Golem || e instanceof Ambient) {
            if (r1 != null && e2 instanceof Player) {
                Player p2 = (Player) e2;
                if (!r1.canInteractPassives(p2)) {
                    e.setCancelled(true);
                    RedProtect.get().getLanguageManager().sendMessage(p2, "entitylistener.region.cantpassive");
                }
            }
        } else if ((e1 instanceof Hanging) && e2 instanceof Player) {
            Player p2 = (Player) e2;
            if (r1 != null && !r1.canBuild(p2)) {
                e.setCancelled(true);
                RedProtect.get().getLanguageManager().sendMessage(p2, "playerlistener.region.cantuse");
                return;
            }
            if (r2 != null && !r2.canBuild(p2)) {
                e.setCancelled(true);
                RedProtect.get().getLanguageManager().sendMessage(p2, "playerlistener.region.cantuse");
            }
        } else if ((e1 instanceof Hanging) && e2 instanceof Monster) {
            if (r1 != null || r2 != null) {
                RedProtect.get().logger.debug(LogLevel.ENTITY, "Cancelled ItemFrame drop Item");
                e.setCancelled(true);
            }
        } else if (e1 instanceof Explosive && !(e1 instanceof Living)) {
            if ((r1 != null && !r1.canFire()) || (r2 != null && !r2.canFire())) {
                e.setCancelled(true);
            }
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onPotionSplash(LaunchProjectileEvent event) {

        if (event.getTargetEntity() instanceof ThrownPotion) {
            ThrownPotion potion = (ThrownPotion) event.getTargetEntity();
            ProjectileSource thrower = potion.getShooter();

            RedProtect.get().logger.debug(LogLevel.ENTITY, "EntityListener - LaunchProjectileEvent entity " + event.getTargetEntity().getType().getName());

            List<PotionEffect> pottypes = potion.get(Keys.POTION_EFFECTS).get();
            for (PotionEffect t : pottypes) {
                if (!t.getType().equals(PotionEffectTypes.BLINDNESS) &&
                        !t.getType().equals(PotionEffectTypes.WEAKNESS) &&
                        !t.getType().equals(PotionEffectTypes.NAUSEA) &&
                        !t.getType().equals(PotionEffectTypes.HUNGER) &&
                        !t.getType().equals(PotionEffectTypes.POISON) &&
                        !t.getType().equals(PotionEffectTypes.MINING_FATIGUE) &&
                        !t.getType().equals(PotionEffectTypes.HASTE) &&
                        !t.getType().equals(PotionEffectTypes.SLOWNESS) &&
                        !t.getType().equals(PotionEffectTypes.WITHER)) {
                    return;
                }
            }

            Player shooter;
            if (thrower instanceof Player) {
                shooter = (Player) thrower;
            } else {
                return;
            }

            RedProtect.get().logger.debug(LogLevel.ENTITY, "EntityListener - LaunchProjectileEvent shooter " + shooter.getName());

            Entity e2 = event.getTargetEntity();
            Region r = RedProtect.get().getRegionManager().getTopRegion(e2.getLocation(), this.getClass().getName());
            if (e2 instanceof Player) {
                if (r != null && r.flagExists("pvp") && !r.canPVP(shooter, (Player) e2)) {
                    event.setCancelled(true);
                }
            } else {
                if (r != null && !r.canInteractPassives(shooter)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onInteractEvent(InteractEntityEvent.Secondary e, @First Player p) {
        Entity et = e.getTargetEntity();
        Location<World> l = et.getLocation();
        Region r = RedProtect.get().getRegionManager().getTopRegion(l, this.getClass().getName());

        RedProtect.get().logger.debug(LogLevel.ENTITY, "EntityListener - InteractEntityEvent.Secondary entity " + et.getType().getName());

        if (r != null && !r.canInteractPassives(p) && (et instanceof Animal || et instanceof Villager || et instanceof Golem || et instanceof Ambient)) {
            if (RedProtect.get().getVersionHelper().checkHorseOwner(et, p)) {
                return;
            }
            e.setCancelled(true);
            RedProtect.get().getLanguageManager().sendMessage(p, "entitylistener.region.cantinteract");
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void WitherBlockBreak(ChangeBlockEvent.Break event, @First Entity e) {
        if (e instanceof Monster) {
            BlockSnapshot b = event.getTransactions().get(0).getOriginal();
            RedProtect.get().logger.debug(LogLevel.ENTITY, "EntityListener - Is EntityChangeBlockEvent event! Block " + b.getState().getType().getName());
            Region r = RedProtect.get().getRegionManager().getTopRegion(b.getLocation().get(), this.getClass().getName());
            if (!cont.canWorldBreak(b)) {
                event.setCancelled(true);
                return;
            }
            if (r != null && !r.canMobLoot()) {
                event.setCancelled(true);
            }
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onAttemptInteractAS(InteractEntityEvent e, @First Player p) {

        Entity ent = e.getTargetEntity();
        Location<World> l = ent.getLocation();
        Region r = RedProtect.get().getRegionManager().getTopRegion(l, this.getClass().getName());

        if (r == null) {
            //global flags
            if (ent.getType().equals(EntityTypes.ARMOR_STAND)) {
                if (!RedProtect.get().getConfigManager().globalFlagsRoot().worlds.get(l.getExtent().getName()).build) {
                    e.setCancelled(true);
                    return;
                }
            }
            return;
        }

        ItemType itemInHand = RedProtect.get().getVersionHelper().getItemInHand(p);

        if (!itemInHand.equals(ItemTypes.NONE) && itemInHand.getType().equals(ItemTypes.ARMOR_STAND)) {
            if (!r.canBuild(p)) {
                e.setCancelled(true);
                RedProtect.get().getLanguageManager().sendMessage(p, "blocklistener.region.cantbuild");
                return;
            }
        }

        //TODO Not working!
        if (ent.getType().equals(EntityTypes.ARMOR_STAND)) {
            if (!r.canBuild(p)) {
                if (!RedProtect.get().getPermissionHandler().hasPerm(p, "redprotect.bypass")) {
                    RedProtect.get().getLanguageManager().sendMessage(p, "playerlistener.region.cantedit");
                    e.setCancelled(true);
                }
            }
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onEntityDamage(DamageEntityEvent e, @First Entity e2) {

        Entity e1 = e.getTargetEntity();
        Location<World> loc = e1.getLocation();

        Player damager = null;
        if (e2 instanceof Projectile) {
            Projectile proj = (Projectile) e2;
            if (proj.getShooter() instanceof Player) {
                damager = (Player) proj.getShooter();
            }
        } else if (e2 instanceof Player) {
            damager = (Player) e2;
        }

        Region r1 = RedProtect.get().getRegionManager().getTopRegion(loc, this.getClass().getName());

        if (r1 == null) {
            //global flags
            if (e1 instanceof ArmorStand) {
                if (e2 instanceof Player) {
                    if (!RedProtect.get().getConfigManager().globalFlagsRoot().worlds.get(loc.getExtent().getName()).build) {
                        e.setCancelled(true);
                        return;
                    }
                }
            }
            return;
        }

        if (e1 instanceof ArmorStand) {
            if (!r1.canBuild(damager)) {
                e.setCancelled(true);
                RedProtect.get().getLanguageManager().sendMessage(damager, "blocklistener.region.cantbreak");
            }
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onBlockExplode(ExplosionEvent.Detonate e) {
        RedProtect.get().logger.debug(LogLevel.DEFAULT, "Is BlockListener - BlockExplodeEvent event");

        for (Location<World> bex : e.getAffectedLocations()) {
            Region r = RedProtect.get().getRegionManager().getTopRegion(bex, this.getClass().getName());
            if (!cont.canWorldBreak(bex.createSnapshot())) {
                e.setCancelled(true);
                return;
            }
            if (r != null && !r.canFire()) {
                e.setCancelled(true);
                return;
            }
        }
    }
}
