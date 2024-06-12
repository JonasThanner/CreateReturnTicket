package com.qsmium.createreturnticket;

public class Util
{

    public static int Clamp(int min, int max, int value)
    {
        return Math.max(min, Math.min(max, value));
    }
}
