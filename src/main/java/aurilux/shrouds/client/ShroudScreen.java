package aurilux.shrouds.client;

import aurilux.shrouds.common.PacketHandler;
import aurilux.shrouds.common.PacketUpdateShroud;
import aurilux.shrouds.common.ShroudContainer;
import aurilux.shrouds.common.ShroudTitleEntity;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
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
import net.minecraft.potion.Effects;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ShroudScreen extends ContainerScreen<ShroudContainer> {
    private static final ResourceLocation BEACON_GUI_TEXTURES = new ResourceLocation("textures/gui/container/beacon.png");
    private ShroudScreen.ConfirmButton confirmButton;
    private boolean buttonsNotDrawn;
    private Effect primaryEffect;
    private Effect secondaryEffect;

    public ShroudScreen(final ShroudContainer container, PlayerInventory inventory, ITextComponent component) {
        super(container, inventory, component);
        this.xSize = 230;
        this.ySize = 219;
        container.addListener(new IContainerListener() {
            @Override
            public void sendAllContents(Container containerToSend, NonNullList<ItemStack> itemsList) {
            }

            @Override
            public void sendSlotContents(Container containerToSend, int slotInd, ItemStack stack) {
            }

            @Override
            public void sendWindowProperty(Container containerIn, int varToUpdate, int newValue) {
                ShroudScreen.this.primaryEffect = container.getPrimaryEffect();
                ShroudScreen.this.secondaryEffect = container.getSecondaryEffect();
                ShroudScreen.this.buttonsNotDrawn = true;
            }
        });
    }

    @Override
    protected void init() {
        super.init();
        this.confirmButton = this.addButton(new ShroudScreen.ConfirmButton(this.guiLeft + 164, this.guiTop + 107));
        this.addButton(new ShroudScreen.CancelButton(this.guiLeft + 190, this.guiTop + 107));
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
                int numEffects = ShroudTitleEntity.EFFECTS_LIST[powerLevel].length;
                int xOffset = numEffects * 22 + (numEffects - 1) * 2;

                for (int effectIndex = 0; effectIndex < numEffects; effectIndex++) {
                    Effect effect = ShroudTitleEntity.EFFECTS_LIST[powerLevel][effectIndex];
                    ShroudScreen.PowerButton powerButton = new ShroudScreen.PowerButton(this.guiLeft + 76 + effectIndex * 24 - xOffset / 2, this.guiTop + 22 + powerLevel * 25, effect, true);
                    this.addButton(powerButton);
                    if (powerLevel >= levels) {
                        powerButton.active = false;
                    }
                    else if (effect == this.primaryEffect) {
                        powerButton.setSelected(true);
                    }
                }
            }

            int numEffects = ShroudTitleEntity.EFFECTS_LIST[3].length;
            int xOffset = (numEffects + 1) * 22 + numEffects * 2;

            for (int effectIndex = 0; effectIndex < numEffects; effectIndex++) {
                Effect effect = ShroudTitleEntity.EFFECTS_LIST[3][effectIndex];
                ShroudScreen.PowerButton powerButton = new ShroudScreen.PowerButton(this.guiLeft + 167 + effectIndex * 24 - xOffset / 2, this.guiTop + 47, effect, false);
                this.addButton(powerButton);
                if (3 >= levels) {
                    powerButton.active = false;
                }
                else if (effect == this.secondaryEffect) {
                    powerButton.setSelected(true);
                }
            }

            if (this.primaryEffect != null) {
                ShroudScreen.PowerButton powerButton = new ShroudScreen.PowerButton(this.guiLeft + 167 + numEffects * 24 - xOffset / 2, this.guiTop + 47, this.primaryEffect, false);
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
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        this.drawCenteredString(this.font, I18n.format("block.minecraft.beacon.primary"), 62, 10, 14737632);
        this.drawCenteredString(this.font, I18n.format("block.minecraft.beacon.secondary"), 169, 10, 14737632);

        for (Widget widget : this.buttons) {
            if (widget.isHovered()) {
                widget.renderToolTip(mouseX - this.guiLeft, mouseY - this.guiTop);
                break;
            }
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bindTexture(BEACON_GUI_TEXTURES);
        int xCoord = (this.width - this.xSize) / 2;
        int yCoord = (this.height - this.ySize) / 2;
        this.blit(xCoord, yCoord, 0, 0, this.xSize, this.ySize);
        this.itemRenderer.zLevel = 100.0F;
        this.itemRenderer.renderItemAndEffectIntoGUI(new ItemStack(Items.EMERALD), xCoord + 42, yCoord + 109);
        this.itemRenderer.renderItemAndEffectIntoGUI(new ItemStack(Items.DIAMOND), xCoord + 42 + 22, yCoord + 109);
        this.itemRenderer.renderItemAndEffectIntoGUI(new ItemStack(Items.GOLD_INGOT), xCoord + 42 + 44, yCoord + 109);
        this.itemRenderer.renderItemAndEffectIntoGUI(new ItemStack(Items.IRON_INGOT), xCoord + 42 + 66, yCoord + 109);
        this.itemRenderer.zLevel = 0.0F;
    }

    @Override
    public void render(int mouseX, int mouseZ, float ticks) {
        this.renderBackground();
        super.render(mouseX, mouseZ, ticks);
        this.renderHoveredToolTip(mouseX, mouseZ);
    }

    @OnlyIn(Dist.CLIENT)
    abstract static class Button extends AbstractButton {
        private boolean selected;

        protected Button(int x, int y) {
            super(x, y, 22, 22, "");
        }

        public void renderButton(int backX, int backY, float partial) {
            Minecraft.getInstance().getTextureManager().bindTexture(ShroudScreen.BEACON_GUI_TEXTURES);
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

            this.blit(this.x, this.y, j, 219, this.width, this.height);
            this.blitButton();
        }

        protected abstract void blitButton();

        public boolean isSelected() {
            return this.selected;
        }

        public void setSelected(boolean selectedIn) {
            this.selected = selectedIn;
        }
    }

    @OnlyIn(Dist.CLIENT)
    class PowerButton extends ShroudScreen.Button {
        private final Effect effect;
        private final TextureAtlasSprite textureSprite;
        private final boolean isPrimary;

        public PowerButton(int x, int y, Effect effectIn, boolean primary) {
            super(x, y);
            this.effect = effectIn;
            this.textureSprite = Minecraft.getInstance().getPotionSpriteUploader().getSprite(effectIn);
            this.isPrimary = primary;
        }

        @Override
        public void onPress() {
            if (!this.isSelected()) {
                if (this.isPrimary) {
                    ShroudScreen.this.primaryEffect = this.effect;
                }
                else {
                    ShroudScreen.this.secondaryEffect = this.effect;
                }

                ShroudScreen.this.buttons.clear();
                ShroudScreen.this.children.clear();
                ShroudScreen.this.init();
                ShroudScreen.this.tick();
            }
        }

        @Override
        public void renderToolTip(int x, int y) {
            String s = I18n.format(this.effect.getName());
            if (!this.isPrimary && this.effect != Effects.REGENERATION) {
                s = s + " II";
            }

            ShroudScreen.this.renderTooltip(s, x, y);
        }

        @Override
        protected void blitButton() {
            Minecraft.getInstance().getTextureManager().bindTexture(textureSprite.getAtlasTexture().getTextureLocation());
            blit(this.x + 2, this.y + 2, this.getBlitOffset(), 18, 18, this.textureSprite);
        }
    }

    @OnlyIn(Dist.CLIENT)
    abstract static class SpriteButton extends ShroudScreen.Button {
        private final int offsetX;
        private final int offsetY;

        protected SpriteButton(int x, int y, int offsetX, int offsetY) {
            super(x, y);
            this.offsetX = offsetX;
            this.offsetY = offsetY;
        }

        @Override
        protected void blitButton() {
            this.blit(this.x + 2, this.y + 2, this.offsetX, this.offsetY, 18, 18);
        }
    }

    @OnlyIn(Dist.CLIENT)
    class ConfirmButton extends ShroudScreen.SpriteButton {
        public ConfirmButton(int x, int y) {
            super(x, y, 90, 220);
        }

        @Override
        public void onPress() {
            PacketHandler.sendToServer(new PacketUpdateShroud(Effect.getId(ShroudScreen.this.primaryEffect), Effect.getId(ShroudScreen.this.secondaryEffect)));
            ShroudScreen.this.minecraft.player.connection.sendPacket(new CCloseWindowPacket(ShroudScreen.this.minecraft.player.openContainer.windowId));
            ShroudScreen.this.minecraft.displayGuiScreen(null);
        }

        @Override
        public void renderToolTip(int x, int y) {
            ShroudScreen.this.renderTooltip(I18n.format("gui.done"), x, y);
        }
    }

    @OnlyIn(Dist.CLIENT)
    class CancelButton extends ShroudScreen.SpriteButton {
        public CancelButton(int x, int y) {
            super(x, y, 112, 220);
        }

        public void onPress() {
            ShroudScreen.this.minecraft.player.connection.sendPacket(new CCloseWindowPacket(ShroudScreen.this.minecraft.player.openContainer.windowId));
            ShroudScreen.this.minecraft.displayGuiScreen(null);
        }

        public void renderToolTip(int x, int y) {
            ShroudScreen.this.renderTooltip(I18n.format("gui.cancel"), x, y);
        }
    }
}