package com.nihit.craft_connect.utils;

public class Capital {
    private Capital() {

    }

    public static String capitalizeWord(String str) {
        return String.valueOf(str.charAt(0)).toUpperCase() + str.substring(1, str.length());
    }
}
