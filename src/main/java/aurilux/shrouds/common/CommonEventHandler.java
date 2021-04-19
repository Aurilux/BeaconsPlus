package aurilux.shrouds.common;

import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.monster.EndermanEntity;
import net.minecraft.entity.monster.ShulkerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ShroudsMod.ID)
public class CommonEventHandler {
    @SubscribeEvent
    public static void onEnderTeleport(EnderTeleportEvent event) {
        LivingEntity entity = event.getEntityLiving();
        if (entity.isPotionActive(ModObjects.BINDING.get())) {
            int amplifier = entity.getActivePotionEffect(ModObjects.BINDING.get()).getAmplifier();
            if (entity instanceof EndermanEntity || (entity instanceof ShulkerEntity && amplifier > 0)) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onCriticalHit(CriticalHitEvent event) {
        if (!(event.getTarget() instanceof LivingEntity)) {
            return;
        }

        LivingEntity entity = (LivingEntity) event.getTarget();
        if (entity.isPotionActive(ModObjects.FRAILTY.get())) {
            event.setDamageModifier(event.getDamageModifier()
                    + (.5f * (entity.getActivePotionEffect(ModObjects.FRAILTY.get()).getAmplifier() + 1)));
        }
    }

    /*
    A creeper spawns a cloud effect on death that applies all potions that was affecting it to entities that walk
    through it. I remove the effects added by a shroud to prevent abuse, and just to make it safer for players.
    Because if a creeper was affected by all the effects a shroud can produce, the cloud would be an enormous problem.
    */
    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinWorldEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof AreaEffectCloudEntity) {
            AreaEffectCloudEntity effectCloud = (AreaEffectCloudEntity) entity;
            effectCloud.effects.removeIf(EffectInstance::isAmbient);
            if (effectCloud.effects.size() <= 0) {
                event.setCanceled(true);
            }
        }
    }

    /*
    Why this and not a cancel of the ExplosionEvent.Start event? Because I still want the sound and particle effects.
    */
    @SubscribeEvent
    public static void onCreeperDetonate(ExplosionEvent.Detonate event) {
        Entity exploder = event.getExplosion().getExploder();
        if (exploder instanceof CreeperEntity && ((CreeperEntity) exploder).isPotionActive(ModObjects.DAMPEN.get())) {
            event.getAffectedBlocks().clear();
            event.getAffectedEntities().clear();
        }
    }

    public static float nauseaInaccuracy(ProjectileEntity projectile) {
        // Get the owner/shooter of the projectile
        LivingEntity entity = (LivingEntity) projectile.func_234616_v_();

        // Players already have a hard time with the screen warping effect of nausea. No need to punish them further.
        if (entity != null && !(entity instanceof PlayerEntity) && entity.isPotionActive(Effects.NAUSEA)) {
            return 1 + entity.getActivePotionEffect(Effects.NAUSEA).getAmplifier();
        }
        return 0.0f;
    }
}