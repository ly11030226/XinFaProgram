package com.ads.xinfa.utils;

public class ColorTool {

    public String colorChange(String color) {
            if(color.length()==3){
                color="#"+color.charAt(0)+color.charAt(0)+color.charAt(1)+color.charAt(1)+color.charAt(2)+color.charAt(2);
            }else {
                color="#"+color;
            }
            return  color;
    }
}
