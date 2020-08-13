package aurilux.shrouds.common;

import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;

import javax.annotation.Nonnull;

public class PotionWeight extends Effect {
    public PotionWeight() {
        super(EffectType.HARMFUL, 0x000000);
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return true;
    }

    @Override
    public void performEffect(@Nonnull LivingEntity living, int amplified) {
    }
}