package aurilux.shrouds.common;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.tags.BlockTags;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.LockableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.LockCode;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class ShroudTitleEntity extends TileEntity implements INamedContainerProvider, ITickableTileEntity {
    public static final Effect[][] EFFECTS_LIST = new Effect[][]{
            {Effects.WEAKNESS, Effects.NAUSEA},
            {Effects.SLOWNESS, ModObjects.BINDING.get()},
            {ModObjects.FRAILTY.get()},
            {ModObjects.DAMPEN.get()}};
    private static final Set<Effect> VALID_EFFECTS = Arrays.stream(EFFECTS_LIST).flatMap(Arrays::stream).collect(Collectors.toSet());
    private List<ShroudTitleEntity.BeamSegment> beamSegments = Lists.newArrayList();
    private List<ShroudTitleEntity.BeamSegment> segmentSize = Lists.newArrayList();
    private int levels = 0;
    private int beamHeight = -1;
    private Effect primaryEffect;
    private Effect secondaryEffect;
    private ITextComponent customName;
    private LockCode lock = LockCode.EMPTY_CODE;
    private final IIntArray intArray = new IIntArray() {
        public int get(int index) {
            switch(index) {
                case 0:
                    return ShroudTitleEntity.this.levels;
                case 1:
                    return Effect.getId(ShroudTitleEntity.this.primaryEffect);
                case 2:
                    return Effect.getId(ShroudTitleEntity.this.secondaryEffect);
                default:
                    return 0;
            }
        }

        public void set(int index, int value) {
            switch(index) {
                case 0:
                    ShroudTitleEntity.this.levels = value;
                    break;
                case 1:
                    if (!(ShroudTitleEntity.this.world != null && ShroudTitleEntity.this.world.isRemote) && !ShroudTitleEntity.this.beamSegments.isEmpty()) {
                        ShroudTitleEntity.this.playSound(SoundEvents.BLOCK_BEACON_POWER_SELECT);
                    }
                    ShroudTitleEntity.this.primaryEffect = ShroudTitleEntity.isBeaconEffect(value);
                    break;
                case 2:
                    ShroudTitleEntity.this.secondaryEffect = ShroudTitleEntity.isBeaconEffect(value);
            }

        }

        public int size() {
            return 3;
        }
    };

    public ShroudTitleEntity() {
        super(ModObjects.SHROUD_TILE.get());
    }

    @Override
    public void tick() {
        int x = this.pos.getX();
        int y = this.pos.getY();
        int z = this.pos.getZ();
        BlockPos blockpos;
        if (this.beamHeight < y) {
            blockpos = this.pos;
            this.segmentSize = Lists.newArrayList();
            this.beamHeight = blockpos.getY() - 1;
        }
        else {
            blockpos = new BlockPos(x, this.beamHeight + 1, z);
        }

        ShroudTitleEntity.BeamSegment beaconBeamSegment = this.segmentSize.isEmpty() ? null : this.segmentSize.get(this.segmentSize.size() - 1);
        int height = this.world != null ? this.world.getHeight(Heightmap.Type.WORLD_SURFACE, x, z) : 0;

        for(int i = 0; i < 10 && blockpos.getY() <= height; i++) {
            BlockState blockstate = this.world.getBlockState(blockpos);
            Block block = blockstate.getBlock();
            float[] colors = blockstate.getBeaconColorMultiplier(this.world, blockpos, getPos());
            if (colors != null) {
                if (this.segmentSize.size() <= 1) {
                    beaconBeamSegment = new ShroudTitleEntity.BeamSegment(colors);
                    this.segmentSize.add(beaconBeamSegment);
                }
                else if (beaconBeamSegment != null) {
                    if (Arrays.equals(colors, beaconBeamSegment.colors)) {
                        beaconBeamSegment.incrementHeight();
                    }
                    else {
                        beaconBeamSegment = new ShroudTitleEntity.BeamSegment(new float[]{(beaconBeamSegment.colors[0] + colors[0]) / 2.0F, (beaconBeamSegment.colors[1] + colors[1]) / 2.0F, (beaconBeamSegment.colors[2] + colors[2]) / 2.0F});
                        this.segmentSize.add(beaconBeamSegment);
                    }
                }
            }
            else {
                if (beaconBeamSegment == null || blockstate.getOpacity(this.world, blockpos) >= 15 && block != Blocks.BEDROCK) {
                    this.segmentSize.clear();
                    this.beamHeight = height;
                    break;
                }
                beaconBeamSegment.incrementHeight();
            }

            blockpos = blockpos.up();
            this.beamHeight++;
        }

        int levelsBefore = this.levels;
        if (this.world.getGameTime() % 80L == 0L) {
            if (!this.beamSegments.isEmpty()) {
                this.checkBeaconLevel(x, y, z);
            }

            if (this.levels > 0 && !this.beamSegments.isEmpty()) {
                this.addEffectsToEntities();
                this.playSound(SoundEvents.BLOCK_BEACON_AMBIENT);
            }
        }

        if (this.beamHeight >= height) {
            this.beamHeight = -1;
            boolean flag = levelsBefore > 0;
            this.beamSegments = this.segmentSize;
            if (!this.world.isRemote) {
                // this.levels will have changed after calling "this.sendBeam" as above
                boolean flag1 = this.levels > 0;
                if (!flag && flag1) {
                    this.playSound(SoundEvents.BLOCK_BEACON_ACTIVATE);
                }
                else if (flag && !flag1) {
                    this.playSound(SoundEvents.BLOCK_BEACON_DEACTIVATE);
                }
            }
        }
    }

    private void checkBeaconLevel(int posX, int posY, int posZ) {
        this.levels = 0;
        for(int i = 1; i <= 4; this.levels = i++) {
            int y = posY - i;
            if (y < 0) {
                break;
            }

            boolean beaconVerified = true;
            for(int x = posX - i; x <= posX + i && beaconVerified; x++) {
                for(int z = posZ - i; z <= posZ + i; z++) {
                    if (!this.world.getBlockState(new BlockPos(x, y, z)).isIn(BlockTags.BEACON_BASE_BLOCKS)) {
                        beaconVerified = false;
                        break;
                    }
                }
            }

            if (!beaconVerified) {
                break;
            }
        }
    }

    @Override
    public void remove() {
        this.playSound(SoundEvents.BLOCK_BEACON_DEACTIVATE);
        super.remove();
    }

    private void addEffectsToEntities() {
        if (!this.world.isRemote && this.primaryEffect != null) {
            double radius = (this.levels * 10) + 10;

            AxisAlignedBB axisalignedbb = new AxisAlignedBB(this.pos).grow(radius).expand(0.0D, this.world.getHeight(), 0.0D);
            List<CreatureEntity> list = this.world.getEntitiesWithinAABB(CreatureEntity.class, axisalignedbb);
            // We only want to affect "monsters" (generally the hostile kind), and non-bosses
            list.removeIf(c -> c.getType().getClassification() != EntityClassification.MONSTER || !c.isNonBoss());

            int duration = 60;
            int amplifier = (this.levels >= 4 && this.primaryEffect == this.secondaryEffect) ? 1 : 0;

            for(CreatureEntity creatureEntity : list) {
                creatureEntity.addPotionEffect(new EffectInstance(this.primaryEffect, duration, amplifier, true, true));
            }

            if (this.levels >= 4 && this.secondaryEffect != null && this.primaryEffect != this.secondaryEffect) {
                for(CreatureEntity creatureEntity : list) {
                    creatureEntity.addPotionEffect(new EffectInstance(this.secondaryEffect, duration, 0, true, true));
                }
            }
        }
    }

    public void playSound(SoundEvent event) {
        if (this.world != null) {
            this.world.playSound(null, this.pos, event, SoundCategory.BLOCKS, 1.0F, 1.0F);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public List<ShroudTitleEntity.BeamSegment> getBeamSegments() {
        return this.levels == 0 ? ImmutableList.of() : this.beamSegments;
    }

    @Override
    @Nullable
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(this.pos, 3, this.getUpdateTag());
    }

    @Nonnull
    @Override
    public CompoundNBT getUpdateTag() {
        return this.write(new CompoundNBT());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return 65536.0D;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return INFINITE_EXTENT_AABB;
    }

    @Nullable
    private static Effect isBeaconEffect(int value) {
        Effect effect = Effect.get(value);
        return VALID_EFFECTS.contains(effect) ? effect : null;
    }

    @Override
    public void read(@Nonnull BlockState state, @Nonnull CompoundNBT compound) {
        super.read(state, compound);
        this.primaryEffect = isBeaconEffect(compound.getInt("Primary"));
        this.secondaryEffect = isBeaconEffect(compound.getInt("Secondary"));
        if (compound.contains("CustomName", 8)) {
            this.customName = ITextComponent.Serializer.getComponentFromJson(compound.getString("CustomName"));
        }

        this.lock = LockCode.read(compound);
    }

    @Nonnull
    @Override
    public CompoundNBT write(@Nonnull CompoundNBT compound) {
        super.write(compound);
        compound.putInt("Primary", Effect.getId(this.primaryEffect));
        compound.putInt("Secondary", Effect.getId(this.secondaryEffect));
        compound.putInt("Levels", this.levels);
        if (this.customName != null) {
            compound.putString("CustomName", ITextComponent.Serializer.toJson(this.customName));
        }

        this.lock.write(compound);
        return compound;
    }

    public void setCustomName(@Nullable ITextComponent aname) {
        this.customName = aname;
    }

    @Override
    @Nullable
    public Container createMenu(int id, @Nonnull PlayerInventory playerInv, @Nonnull PlayerEntity player) {
        return LockableTileEntity.canUnlock(player, this.lock, this.getDisplayName()) ? new ShroudContainer(id, playerInv, this.intArray, IWorldPosCallable.of(Objects.requireNonNull(this.world), this.getPos())) : null;
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName() {
        return this.customName != null ? this.customName : new TranslationTextComponent("block.shrouds.shroud");
    }

    public static class BeamSegment {
        private final float[] colors;
        private int height;

        public BeamSegment(float[] colorsIn) {
            this.colors = colorsIn;
            this.height = 1;
        }

        protected void incrementHeight() {
            ++this.height;
        }

        /**
         * Returns RGB (0 to 1.0) colors of this beam segment
         */
        @OnlyIn(Dist.CLIENT)
        public float[] getColors() {
            return this.colors;
        }

        @OnlyIn(Dist.CLIENT)
        public int getHeight() {
            return this.height;
        }
    }
}