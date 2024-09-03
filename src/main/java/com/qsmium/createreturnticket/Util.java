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

