package com.qsmium.createreturnticket.gui;


import com.mojang.blaze3d.vertex.PoseStack;
import com.qsmium.createreturnticket.ModMain;
import com.qsmium.createreturnticket.mixins.InventoryMixin;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import org.jline.reader.Widget;

import java.util.List;

public class ReturnTicketWidget extends AbstractWidget implements Widget, GuiEventListener
{

    public static final ResourceLocation TEXTURE = new ResourceLocation(ModMain.MODID,"textures/return_ticket.png");
    private final Minecraft client;
    private final int x;
    private final int y;
    private final int width;
    private final int height;

    private boolean active = false;
    //private final List<Button> buttons = new ArrayList<>();


    public ReturnTicketWidget(int x, int y, int width, int height, Minecraft client) {
        super(x, y, width, height, null);
        this.client = client;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;




//        buttons.add(new AlwaysOnTopTexturedButtonWidget(x + 3, y + 46, 24, 8, 37, 0, 16, TEXTURE, button -> {
//            if (Screen.hasShiftDown() && Screen.hasControlDown()) {
//                NetworkHandler.INSTANCE.sendToServer(RequestPurseActionC2SPacket.extractAll());
//            } else if (selectedValue() > 0) {
//                NetworkHandler.INSTANCE.sendToServer(RequestPurseActionC2SPacket.extract(selectedValue()));
//                resetSelectedValue();
//            }
//        }));

    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta)
    {
        if (!active) return;

        // Bind the texture
        //Minecraft.getInstance().getTextureManager().bindTexture(TEXTURE);

        // Draw the texture using blit
        graphics.blit(TEXTURE, this.x, this.y, 0, 0, this.width, this.height);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput p_259858_)
    {

    }



//    @Override
//    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
//        if (!active) return;
//
//        //Draw over items in the crafting interface
//        RenderSystem.disableDepthTest();
//        RenderSystem.setShaderTexture(0, TEXTURE);
//        blit(matrices, x, y, 0, 0, 37, 60);
//
//        for (Button button : buttons) {
//            button.render(matrices, mouseX, mouseY, delta);
//        }
//
//        client.font.draw(matrices, Component.literal("" + goldAmount), x + 5, y + 12, 16777215);
//        client.font.draw(matrices, Component.literal("" + silverAmount), x + 5, y + 24, 16777215);
//        client.font.draw(matrices, Component.literal("" + bronzeAmount), x + 5, y + 36, 16777215);
//    }

//    @Override
//    public boolean mouseClicked(double mouseX, double mouseY, int button) {
//        if (!this.active || client.player.isSpectator()) return false;
//
//        for (Button buttonWidget : buttons) {
//            if (buttonWidget.mouseClicked(mouseX, mouseY, button)) return true;
//        }
//
//        return isMouseOver(mouseX, mouseY);
//    }

//    //Required to not draw tooltips for items in the crafting interface
//    @Override
//    public boolean isMouseOver(double mouseX, double mouseY) {
//        return mouseX >= x && mouseX <= x + 37 && mouseY >= y && mouseY <= y + 57 && active;
//    }

    public void toggleActive() {
        active = !active;
    }

    @Override
    public boolean apply()
    {
        return false;
    }

//    /**
//     * Modifies a value by either 1 or 10 depending on whether or not SHIFT is held
//     * <br>
//     * Shortcut for {@link PurseWidget#modifyInBounds(MutableInt, int, boolean, Currency)}
//     */
//    private void modifyInBounds(MutableInt value, boolean add, Currency currency) {
//        modifyInBounds(value, Screen.hasShiftDown() ? 10 : 1, add, currency);
//    }


//    /**
//     * Modifies a value with respect to the total amount of money the player has.
//     * If modifying the value even by one would surpass the player's worth when added
//     * to the two other selected values, nothing will happen
//     *
//     * @param value    The value to modify
//     * @param modifyBy The amount to modify by
//     * @param add      Whether to add or subtract
//     * @param currency The currency this selector is for
//     */
//    private void modifyInBounds(MutableInt value, int modifyBy, boolean add, Currency currency) {
//
//        //Get the step size of this selector
//        long stepSize = currency.getRawValue(1);
//
//        //Calculate possible steps using the difference between the player's worth and the currently selected values added together
//        long possibleSteps = (currencyStorage.getValue() - selectedValue()) / stepSize;
//
//        //Upper bound is either 99 or the current value of this selector plus the possible steps
//        int upperBound = CurrencyConverter.asInt(Math.min(value.intValue() + possibleSteps, 99));
//
//        if (add) value.add(modifyBy);
//        else value.subtract(modifyBy);
//
//        if (value.intValue() < 0) value.setValue(0);
//        if (value.intValue() > upperBound) value.setValue(upperBound);
//    }

//    /**
//     * Resolves the selected values into a raw currency value
//     *
//     * @return The raw value of all selectors added with respect to their different worths
//     */
//    private long selectedValue() {
//        return CurrencyResolver.combineValues(new long[]{bronzeAmount.getValue(), silverAmount.getValue(), goldAmount.getValue()});
//    }

//    /**
//     * This adjusts the extract values to the maximum you can do by simply setting
//     * them to zero and then letting them run into bounds
//     */
//    private void resetSelectedValue() {
//
//        //Silently modify client cache because this runs before the sync packet is received
//        currencyStorage.silentModify(-selectedValue());
//
//        int oldGoldAmount = goldAmount.intValue();
//        int oldSilverAmount = silverAmount.intValue();
//        int oldBronzeAmount = bronzeAmount.intValue();
//
//        goldAmount.setValue(0);
//        bronzeAmount.setValue(0);
//        silverAmount.setValue(0);
//
//        modifyInBounds(goldAmount, oldGoldAmount, true, Currency.GOLD);
//        modifyInBounds(silverAmount, oldSilverAmount, true, Currency.SILVER);
//        modifyInBounds(bronzeAmount, oldBronzeAmount, true, Currency.BRONZE);
//    }


}