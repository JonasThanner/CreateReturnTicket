package com.qsmium.createreturnticket;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.qsmium.createreturnticket.gui.ReturnTicketWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import org.joml.Quaternionf;
import org.lwjgl.opengl.GL11;

public class Util
{

    public static int Clamp(int min, int max, int value)
    {
        return Math.max(min, Math.min(max, value));
    }

    public static void SafeScale(PoseStack poseStack, float scaleX, float scaleY, float originX, float originY)
    {
        SafeScale(poseStack, scaleX, scaleY, originX, originY, 0, 0);
    }
    public static void SafeScale(PoseStack poseStack, float scaleX, float scaleY, float originX, float originY, float texSizeX, float texSizeY)
    {
        //Adjust for negative scales => fake negative scaling and then offsetting
        float offsetX = scaleX < 0 ? texSizeX * Math.abs(scaleX) : 0;
        float offsetY = scaleY < 0 ? texSizeY * Math.abs(scaleY) : 0;

        //Make scales normal
        scaleX = Math.abs(scaleX);
        scaleY = Math.abs(scaleY);


        //Do all normal translations
        // Translate to the anchor point before scaling
        poseStack.translate(originX * scaleX, -(originY * scaleY), 0);

        // Apply the scaling
        poseStack.scale(scaleX, scaleY, 1.0F);

        // Translate back to the original position after scaling
        poseStack.translate((((offsetX) - originX) / scaleX), ((-(offsetY) + originY) / scaleY), 0);
    }

    //Works with X Scaling
    //TODO: Merge into one function
    public static void SafeScale2(PoseStack poseStack, float scaleX, float scaleY, float originX, float originY, float texSizeX, float texSizeY)
    {
        // Adjust for negative scales => fake negative scaling and then offsetting
        float offsetX = scaleX < 0 ? texSizeX * Math.abs(scaleX) : 0;
        float offsetY = scaleY < 0 ? texSizeY * Math.abs(scaleY) : 0;

        // Normalize scales (to make sure scale values are positive)
        scaleX = Math.abs(scaleX);
        scaleY = Math.abs(scaleY);

        // Translate to the anchor point before scaling
        poseStack.translate(originX, originY, 0);

        // Apply the scaling
        poseStack.scale(scaleX, scaleY, 1.0F);

        // Translate back to the original position after scaling
        // This compensates for both scaling directions
        poseStack.translate((offsetX - originX * scaleX) / scaleX, (offsetY - originY * scaleY) / scaleY, 0);
    }

    public static void SafeScaleFromMiddle(PoseStack poseStack, float scaleX, float scaleY, float originX, float originY, float texSizeX, float texSizeY)
    {
        //To animate from middle we need to take the tex size, multiply by the scaling of x and y
        //Scale, and then translate it by the appropriate amount so its as if it was scaled from the middle
        // => We need to translate the scaled object to the top left by 1/2 of the change in size between the unscaled and scaled object

        //First we do the original translation
        SafeScale2(poseStack, scaleX, scaleY, originX, originY, texSizeX, texSizeY);

        //Then the translation adjustment
        float offsetX = ((texSizeX * scaleX) - (texSizeX)) / 2.0f;
        float offsetY = ((texSizeY * scaleY) - (texSizeY)) / 2.0f;
        poseStack.translate(-offsetX / scaleX, -offsetY / scaleY, 0);
    }

    public static void setupStencilMask()
    {
        // Reset stencil state and clear stencil buffer
        GL11.glDisable(GL11.GL_STENCIL_TEST);
        RenderSystem.stencilMask(-1);
        RenderSystem.clear(GL11.GL_STENCIL_BUFFER_BIT, Minecraft.ON_OSX);

        // Setup for stencil buffer writing
        GL11.glEnable(GL11.GL_STENCIL_TEST);
        RenderSystem.stencilOp(GL11.GL_REPLACE, GL11.GL_KEEP, GL11.GL_KEEP);
        RenderSystem.stencilMask(0xFF);
        RenderSystem.stencilFunc(GL11.GL_EQUAL, 1, 0xFF);
        RenderSystem.colorMask(false, false, false, false);
        RenderSystem.depthMask(false);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
    }

    public static void setupStencilTexture()
    {
        // Setup for color buffer filling
        GL11.glEnable(GL11.GL_STENCIL_TEST);
        RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
        RenderSystem.stencilMask(0);
        RenderSystem.stencilFunc(GL11.GL_EQUAL, 1, 0xFF);
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.depthMask(true);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
    }

    public static void disableStencil()
    {
        GL11.glDisable(GL11.GL_STENCIL_TEST);
    }

    //Draws multiple Repating Blits
    //Tiles them according to how many repeating x and y are wanted
    public static void drawRepatingBlit(GuiGraphics guiGraphics, ResourceLocation texLoc, int xTopLeft, int yTopLeft,int uvTopLeftX, int uvTopLeftY, int uvWidth, int uvHeight, int sourceTexWidth, int sourceTexHeight, int repeatingX, int repeatingY)
    {
        for(int i = 0; i < repeatingX; i++)
        {
            for(int k = 0; k < repeatingY; k++)
            {
                guiGraphics.blit(texLoc, xTopLeft + (i * uvWidth), yTopLeft + (k * uvHeight), uvTopLeftX, uvTopLeftY, uvWidth, uvHeight, sourceTexWidth, sourceTexHeight);
            }
        }


    }
}

