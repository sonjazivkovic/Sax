package com.example.buca.saxmusicplayer.util;

/**
 * Created by Stefan on 11/06/2017.
 */

public class MathUtil {
    public static int getPercentage(int percFor, int percFrom){
        if(percFor == percFrom)
            return 100;
        else
            return (percFor * 100)/percFrom;
    }

    public static int getNumberFromPercentage(int perc, int wholeValue){
        if(perc == 100)
            return wholeValue - 1000;
        else {
            float fPerc = ((float) perc / 100);
            return (int) (wholeValue * fPerc);
        }
    }
}
