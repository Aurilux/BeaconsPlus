package aurilux.beaconsplus.client.screen;

import aurilux.beaconsplus.common.ModObjects;
import aurilux.beaconsplus.common.container.LushContainer;
import aurilux.beaconsplus.common.network.PacketHandler;
import aurilux.beaconsplus.common.network.messages.PacketUpdateLush;
import aurilux.beaconsplus.common.tile.LushTile;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.AbstractButton;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CCloseWindowPacket;
import net.minecraft.potion.Effect;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
public class LushScreen extends ContainerScreen<LushContainer> {
    private static final ResourceLocation BEACON_GUI_TEXTURES = new ResourceLocation("textures/gui/container/beacon.png");
    private LushScreen.ConfirmButton confirmButton;
    private boolean buttonsNotDrawn;
    private Effect primaryEffect;
    private Effect secondaryEffect;

    public LushScreen(final LushContainer container, PlayerInventory inventory, ITextComponent component) {
        super(container, inventory, component);
        this.xSize = 230;
        this.ySize = 219;
        container.addListener(new IContainerListener() {
            @Override
            public void sendAllContents(@Nonnull Container containerToSend, @Nonnull NonNullList<ItemStack> itemsList) {
            }

            @Override
            public void sendSlotContents(@Nonnull Container containerToSend, int slotInd, @Nonnull ItemStack stack) {
            }

            @Override
            public void sendWindowProperty(@Nonnull Container containerIn, int varToUpdate, int newValue) {
                LushScreen.this.primaryEffect = container.getPrimaryEffect();
                LushScreen.this.secondaryEffect = container.getSecondaryEffect();
                LushScreen.this.buttonsNotDrawn = true;
            }
        });
    }

    @Override
    protected void init() {
        super.init();
        this.confirmButton = this.addButton(new LushScreen.ConfirmButton(this.guiLeft + 164, this.guiTop + 107));
        this.addButton(new LushScreen.CancelButton(this.guiLeft + 190, this.guiTop + 107));
        this.buttonsNotDrawn = true;
        this.confirmButton.active = false;
    }

    @Override
    public void tick() {
        super.tick();
        int levels = this.container.getLevels();
        if (this.buttonsNotDrawn && levels >= 0) {
            this.buttonsNotDrawn = false;

            for (int powerLevel = 0; powerLevel < 3; powerLevel++) {
                int numEffects = LushTile.EFFECTS_LIST[powerLevel].length;
                int xOffset = numEffects * 22 + (numEffects - 1) * 2;

                for (int effectIndex = 0; effectIndex < numEffects; effectIndex++) {
                    Effect effect = LushTile.EFFECTS_LIST[powerLevel][effectIndex];
                    LushScreen.PowerButton powerButton = new LushScreen.PowerButton(this.guiLeft + 76 + effectIndex * 24 - xOffset / 2, this.guiTop + 22 + powerLevel * 25, effect, true);
                    this.addButton(powerButton);
                    if (powerLevel >= levels) {
                        powerButton.active = false;
                    }
                    else if (effect == this.primaryEffect) {
                        powerButton.setSelected(true);
                    }
                }
            }

            int numEffects = LushTile.EFFECTS_LIST[3].length;
            int xOffset = (numEffects + 1) * 22 + numEffects * 2;

            for (int effectIndex = 0; effectIndex < numEffects; effectIndex++) {
                Effect effect = LushTile.EFFECTS_LIST[3][effectIndex];
                LushScreen.PowerButton powerButton = new LushScreen.PowerButton(this.guiLeft + 167 + effectIndex * 24 - xOffset / 2, this.guiTop + 47, effect, false);
                this.addButton(powerButton);
                if (3 >= levels) {
                    powerButton.active = false;
                }
                else if (effect == this.secondaryEffect) {
                    powerButton.setSelected(true);
                }
            }

            if (this.primaryEffect != null) {
                LushScreen.PowerButton powerButton = new LushScreen.PowerButton(this.guiLeft + 167 + numEffects * 24 - xOffset / 2, this.guiTop + 47, this.primaryEffect, false);
                this.addButton(powerButton);
                if (3 >= levels) {
                    powerButton.active = false;
                }
                else if (this.primaryEffect == this.secondaryEffect) {
                    powerButton.setSelected(true);
                }
            }
        }

        this.confirmButton.active = this.container.isActive() && this.primaryEffect != null;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(@Nonnull MatrixStack matrixStack, int mouseX, int mouseY) {
        drawCenteredString(matrixStack, this.font, I18n.format("block.minecraft.beacon.primary"), 62, 10, 14737632);
        drawCenteredString(matrixStack, this.font, I18n.format("block.minecraft.beacon.secondary"), 169, 10, 14737632);

        for (Widget widget : this.buttons) {
            if (widget.isHovered()) {
                widget.renderToolTip(matrixStack, mouseX - this.guiLeft, mouseY - this.guiTop);
                break;
            }
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(@Nonnull MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        getMinecraft().getTextureManager().bindTexture(BEACON_GUI_TEXTURES);
        int xCoord = (this.width - this.xSize) / 2;
        int yCoord = (this.height - this.ySize) / 2;
        this.blit(matrixStack, xCoord, yCoord, 0, 0, this.xSize, this.ySize);
        this.itemRenderer.zLevel = 100.0F;
        this.itemRenderer.renderItemAndEffectIntoGUI(new ItemStack(Items.NETHERITE_INGOT), xCoord + 20, yCoord + 109);
        this.itemRenderer.renderItemAndEffectIntoGUI(new ItemStack(Items.EMERALD), xCoord + 42, yCoord + 109);
        this.itemRenderer.renderItemAndEffectIntoGUI(new ItemStack(Items.DIAMOND), xCoord + 64, yCoord + 109);
        this.itemRenderer.renderItemAndEffectIntoGUI(new ItemStack(Items.GOLD_INGOT), xCoord + 86, yCoord + 109);
        this.itemRenderer.renderItemAndEffectIntoGUI(new ItemStack(Items.IRON_INGOT), xCoord + 108, yCoord + 109);
        this.itemRenderer.zLevel = 0.0F;
    }

    @Override
    public void render(@Nonnull MatrixStack matrixStack, int mouseX, int mouseY, float ticks) {
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, ticks);
        this.renderHoveredTooltip(matrixStack, mouseX, mouseY);
    }

    @OnlyIn(Dist.CLIENT)
    abstract static class Button extends AbstractButton {
        private boolean selected;

        protected Button(int x, int y) {
            super(x, y, 22, 22, StringTextComponent.EMPTY);
        }

        public void renderButton(@Nonnull MatrixStack matrixStack, int backX, int backY, float partial) {
            Minecraft.getInstance().getTextureManager().bindTexture(LushScreen.BEACON_GUI_TEXTURES);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            int j = 0;
            if (!this.active) {
                j += this.width * 2;
            }
            else if (this.selected) {
                j += this.width;
            }
            else if (this.isHovered()){
                j += this.width * 3;
            }

            this.blit(matrixStack, this.x, this.y, j, 219, this.width, this.height);
            this.blitButton(matrixStack);
        }

        protected abstract void blitButton(MatrixStack matrixStack);

        public boolean isSelected() {
            return this.selected;
        }

        public void setSelected(boolean selectedIn) {
            this.selected = selectedIn;
        }
    }

    @OnlyIn(Dist.CLIENT)
    class PowerButton extends LushScreen.Button {
        private final Effect effect;
        private final TextureAtlasSprite textureSprite;
        private final boolean isPrimary;
        private final ITextComponent text;

        public PowerButton(int x, int y, Effect effectIn, boolean primary) {
            super(x, y);
            this.effect = effectIn;
            this.textureSprite = Minecraft.getInstance().getPotionSpriteUploader().getSprite(effectIn);
            this.isPrimary = primary;
            this.text = makeText(effectIn, primary);
        }

        private ITextComponent makeText(Effect effect1, boolean primary) {
            IFormattableTextComponent comp = new TranslationTextComponent(effect1.getName());
            if (!primary && effect1 != ModObjects.DAMPEN.get()) {
                comp.appendString(" II");
            }
            return comp;
        }

        @Override
        public void onPress() {
            if (!this.isSelected()) {
                if (this.isPrimary) {
                    LushScreen.this.primaryEffect = this.effect;
                }
                else {
                    LushScreen.this.secondaryEffect = this.effect;
                }

                LushScreen.this.buttons.clear();
                LushScreen.this.children.clear();
                LushScreen.this.init();
                LushScreen.this.tick();
            }
        }

        @Override
        public void renderToolTip(@Nonnull MatrixStack matrixStack, int x, int y) {
            LushScreen.this.renderTooltip(matrixStack, this.text, x, y);
        }

        @Override
        protected void blitButton(MatrixStack matrixStack) {
            Minecraft.getInstance().getTextureManager().bindTexture(textureSprite.getAtlasTexture().getTextureLocation());
            blit(matrixStack, this.x + 2, this.y + 2, this.getBlitOffset(), 18, 18, this.textureSprite);
        }
    }

    @OnlyIn(Dist.CLIENT)
    abstract static class SpriteButton extends LushScreen.Button {
        private final int offsetX;
        private final int offsetY;

        protected SpriteButton(int x, int y, int offsetX, int offsetY) {
            super(x, y);
            this.offsetX = offsetX;
            this.offsetY = offsetY;
        }

        @Override
        protected void blitButton(MatrixStack matrixStack) {
            this.blit(matrixStack, this.x + 2, this.y + 2, this.offsetX, this.offsetY, 18, 18);
        }
    }

    @OnlyIn(Dist.CLIENT)
    class ConfirmButton extends LushScreen.SpriteButton {
        public ConfirmButton(int x, int y) {
            super(x, y, 90, 220);
        }

        @Override
        public void onPress() {
            PacketHandler.sendToServer(new PacketUpdateLush(Effect.getId(LushScreen.this.primaryEffect), Effect.getId(LushScreen.this.secondaryEffect)));
            LushScreen.this.minecraft.player.connection.sendPacket(new CCloseWindowPacket(LushScreen.this.minecraft.player.openContainer.windowId));
            LushScreen.this.minecraft.displayGuiScreen(null);
        }

        @Override
        public void renderToolTip(@Nonnull MatrixStack matrixStack, int x, int y) {
            LushScreen.this.renderTooltip(matrixStack, DialogTexts.GUI_DONE, x, y);
        }
    }

    @OnlyIn(Dist.CLIENT)
    class CancelButton extends LushScreen.SpriteButton {
        public CancelButton(int x, int y) {
            super(x, y, 112, 220);
        }

        public void onPress() {
            LushScreen.this.minecraft.player.connection.sendPacket(new CCloseWindowPacket(LushScreen.this.minecraft.player.openContainer.windowId));
            LushScreen.this.minecraft.displayGuiScreen(null);
        }

        @Override
        public void renderToolTip(@Nonnull MatrixStack matrixStack, int x, int y) {
            LushScreen.this.renderTooltip(matrixStack, DialogTexts.GUI_CANCEL, x, y);
        }
    }
}