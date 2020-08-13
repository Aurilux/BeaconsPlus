package aurilux.shrouds.common;

import net.minecraft.entity.IProjectile;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.ThrowableEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ShroudsMod.ID)
public class CommonEventHandler {
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity entity = event.getEntityLiving();
        if (event.getSource().equals(DamageSource.FALL) && entity.isPotionActive(ModObjects.WEIGHT.get())) {
            EffectInstance weightEffect = entity.getActivePotionEffect(ModObjects.WEIGHT.get());
            event.setAmount(event.getAmount() * (2 + weightEffect.getAmplifier()));
        }
    }

    @SubscribeEvent
    public static void onPotionEffect(PotionEvent.PotionAddedEvent event) {
        LivingEntity entity = event.getEntityLiving();
        EffectInstance effect = event.getPotionEffect();
        if (entity instanceof IMob && effect.getPotion().equals(Effects.BLINDNESS)) {
            effect.getPotion().addAttributesModifier(SharedMonsterAttributes.FOLLOW_RANGE,
                    "ed8873f3-8281-4170-aa18-87a65805b318",
                    -.5D - (.25 * effect.getAmplifier()),
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
        }
    }

    public static float nauseaInaccuracy(IProjectile projectile) {
        LivingEntity entity = null;
        if (projectile instanceof ThrowableEntity) {
            ThrowableEntity te = (ThrowableEntity) projectile;
            entity = te.getThrower();
        }
        else if (projectile instanceof AbstractArrowEntity) {
            AbstractArrowEntity aae = (AbstractArrowEntity) projectile;
            if (aae.getShooter() instanceof LivingEntity) {
                entity = (LivingEntity) aae.getShooter();
            }
        }


        if (entity != null && entity.isPotionActive(Effects.NAUSEA)) {
            return 1 + entity.getActivePotionEffect(Effects.NAUSEA).getAmplifier();
        }
        return 0.0f;
    }
}