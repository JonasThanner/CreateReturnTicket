package com.qsmium.createreturnticket;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import com.qsmium.createreturnticket.gui.ReturnTicketWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import org.joml.Quaternionf;
import org.lwjgl.opengl.GL11;

import java.util.List;
import java.util.Random;

public class Util
{
    private static final Random RANDOM = new Random();

    public static int Clamp(int min, int max, int value)
    {
        return Math.max(min, Math.min(max, value));
    }

    public static float Clamp(float min, float max, float value)
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
        setupStencilTexture(GL11.GL_KEEP);
    }

    public static void setupStencilTexture(int equalCond)
    {
        // Setup for color buffer filling
        GL11.glEnable(GL11.GL_STENCIL_TEST);
        RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
        RenderSystem.stencilMask(0);
        RenderSystem.stencilFunc(equalCond, 1, 0xFF);
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

    public static int easeInOut(int start, int end, float progress) {
        // Ensure progress is between 0 and 1
        progress = Math.max(0, Math.min(progress, 1));

        // Apply cubic easing: ease-in and ease-out
        if (progress < 0.5) {
            // First half (ease-in)
            progress = 4 * progress * progress * progress;
        } else {
            // Second half (ease-out)
            progress = 1 - (float) Math.pow(-2 * progress + 2, 3) / 2;
        }

        // Interpolate between start and end values
        return Math.round(start + (end - start) * progress);
    }

    public static float easeInOutFloat(float start, float end, float progress) {
        // Ensure progress is between 0 and 1
        progress = Math.max(0, Math.min(progress, 1));

        // Apply cubic easing: ease-in and ease-out
        if (progress < 0.5) {
            // First half (ease-in)
            progress = 4 * progress * progress * progress;
        } else {
            // Second half (ease-out)
            progress = 1 - (float) Math.pow(-2 * progress + 2, 3) / 2;
        }

        // Interpolate between start and end values
        return start + (end - start) * progress;
    }

    public static float cubicInterpolation(List<Pair<Double, Double>> points, double progress)
    {
        progress = Clamp(0.0f, 1.0f, (float)progress);

        int startIndex = points.size() - 2;
        for (int i = 1; i < points.size() - 1; i++)
        {
            if (points.get(i).getFirst() > progress )
            {
                startIndex = i - 1;
                break;
            }
        }

        Pair<Double, Double> p0 = points.get(startIndex);
        Pair<Double, Double> p1 = points.get(startIndex + 1);

        //Normalize Progress
        double dist = p1.getFirst() - p0.getFirst();
        double normalizedProgress = (progress - p0.getFirst()) / dist;

        return easeInOutFloat((float)(double)p0.getSecond(), (float)(double)p1.getSecond(), (float)(double)normalizedProgress);


    }

    // Cubic interpolation function
    public static double CatmullRomInterpolate(List<Pair<Double, Double>> points, double progress) {
        if (points.size() < 4) {
            throw new IllegalArgumentException("Need at least 4 points for cubic interpolation.");
        }

        // Ensure the progress is between 0 and 1
        progress = Clamp(0.0f, 1.0f, (float)progress);

        // Find the first point with a progress greater than the current progress
        int startIndex = 0;
//        for (int i = 1; i < points.size(); i++)
//        {
//            if (points.get(i).getFirst() > progress && i < points.size() - 2)
//            {
//                startIndex = i - 1;
//                break;
//            }
//
//            if(i >= points.size() - 2)
//            {
//                startIndex = points.size() - 4;
//                break;
//            }
//        }

        // Get the relevant points for cubic interpolation
        Pair<Double, Double> p0 = points.get(startIndex);
        Pair<Double, Double> p1 = points.get(startIndex + 1);
        Pair<Double, Double> p2 = points.get(startIndex + 2);
        Pair<Double, Double> p3 = points.get(startIndex + 3);

        if (p0 == null || p1 == null || p2 == null || p3 == null) {
            throw new IllegalArgumentException("Invalid progress range for cubic interpolation.");
        }

        // Normalize the progress between p1 and p2
        double t = (progress - p1.getFirst()) / (p2.getFirst() - p1.getFirst());

        // Apply cubic interpolation (Catmull-Rom)
        return catmullRom(p0.getSecond(), p1.getSecond(), p2.getSecond(), p3.getSecond(), t);
    }

    // Catmull-Rom cubic interpolation formula
    private static double catmullRom(double v0, double v1, double v2, double v3, double t) {
        double t2 = t * t;
        double t3 = t2 * t;

        return 0.5 * ((2 * v1) + (-v0 + v2) * t +
                (2 * v0 - 5 * v1 + 4 * v2 - v3) * t2 +
                (-v0 + 3 * v1 - 3 * v2 + v3) * t3);
    }

    public static boolean insideBoundsUI(int x, int y, int boundsXTopLeft, int boundsYTopLeft, int boundsXBottomRight, int boundsYBottomRight)
    {
        return x >= boundsXTopLeft && x <= boundsXBottomRight && y >= boundsYTopLeft && y <= boundsYBottomRight;
    }

    public static float randomRange(float min, float max)
    {
        if (min > max) {
            float store = max;
            max = min;
            min = store;
        }
        return min + RANDOM.nextFloat() * (max - min);
    }

    public static boolean isNight() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null) {
            long timeOfDay = minecraft.level.getDayTime() % 24000; // Minecraft's day is 24000 ticks
            return timeOfDay >= 13000 && timeOfDay <= 23000; // Night is from tick 13000 to 23000
        }
        return false; // Return false if no level is loaded
    }

    public static Pair<String, String> SeperateString(String text, String seperator)
    {
        int index = text.indexOf(seperator);

        if(index == -1)
        {
            return new Pair<String, String>(text, "");
        }

        return new Pair<String, String>(text.substring(0, index), text.substring(index + seperator.length()));
    }




}

