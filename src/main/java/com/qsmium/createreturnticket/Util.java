package com.qsmium.createreturnticket;

import org.joml.Quaternionf;

public class Util
{

    public static int Clamp(int min, int max, int value)
    {
        return Math.max(min, Math.min(max, value));
    }

    public static Quaternionf angleAxisToQuaternion(double angle, double axisX, double axisY, double axisZ) {
        // Normalize the axis to ensure it's a unit vector
        double magnitude = Math.sqrt(axisX * axisX + axisY * axisY + axisZ * axisZ);
        double normAxisX = axisX / magnitude;
        double normAxisY = axisY / magnitude;
        double normAxisZ = axisZ / magnitude;

        // Calculate the half-angle
        double halfAngle = angle / 2.0;
        double sinHalfAngle = Math.sin(halfAngle);
        double cosHalfAngle = Math.cos(halfAngle);

        // Compute the quaternion components
        double w = cosHalfAngle;
        double x = normAxisX * sinHalfAngle;
        double y = normAxisY * sinHalfAngle;
        double z = normAxisZ * sinHalfAngle;

        return new Quaternionf(w, x, y, z);
    }
}

