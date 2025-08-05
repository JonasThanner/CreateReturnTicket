package com.qsmium.createreturnticket.gui;

public class DimToIconOrText
{
    //This class is responsible for returning:
    // - Does the requested dimension have an associated icon
    // - Return the icon of the associated dimension => Returns an int which gets interpreted as an offset in the texture

    //This class just returns wehter an icon even exists
    // => Would be cool to do this dynamically, but for now its just a static switch case
    //
    //False return is -1
    public static int DoesDimHaveIcon(String dimName)
    {
        switch(dimName)
        {
            case "minecraft:overworld":
                return 0;

            case "minecraft:the_nether":
                return 1;

            case "minecraft:the_end":
                return 2;


        }

        return -1;
    }

}
