package com.balancedbytes.tool.work;

public final class Util {

    public static String getVariableTimeFormat(int minutesTotal) {
        String duration = "";
        int hours = minutesTotal / 60;
        int minutes = minutesTotal - (hours * 60);
        if (hours > 0) {
            duration += hours + "h";
        }
        if ((minutes > 0) || (hours == 0)) {
            duration += minutes + "min";
        }
        return duration;
    }

    public static String getFixedTimeFormat(int minutesTotal) {
        String duration = "";
        int hours = minutesTotal / 60;
        int minutes = minutesTotal - (hours * 60);
        if (hours < 10) {
            duration += "0";
        }
        duration += hours + ":";
        if (minutes < 10) {
            duration += "0";
        }
        duration += minutes + "h";
        return duration;
    }

}
