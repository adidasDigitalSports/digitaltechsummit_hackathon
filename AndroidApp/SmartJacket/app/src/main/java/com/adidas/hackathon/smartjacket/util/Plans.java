package com.adidas.hackathon.smartjacket.util;

import android.graphics.Point;

public class Plans {

    /**
     * List of known training plans.
     */
    public static String jsonStringPlanOne = "{\"na\":\"Plan One\",\"ph\":[{\"du\":5000,\"na\":\"Phase One\",\"in\":30,\"co\":\"r\"},{\"du\":10000,\"na\":\"Phase Two\",\"in\":50,\"co\":\"g\"},{\"du\":12000,\"na\":\"Phase Three\",\"in\":40,\"co\":\"b\"},{\"du\":8000,\"na\":\"Phase Four\",\"in\":20,\"co\":\"g\"}],\"it\":1}";
    public static String jsonStringPlanTwo = "{\"na\":\"Plan Two\",\"ph\":[{\"du\":5000,\"na\":\"Phase One\",\"in\":50,\"co\":\"y\"},{\"du\":2000,\"na\":\"Phase Two\",\"in\":100,\"co\":\"b\"},{\"du\":7000,\"na\":\"Phase Three\",\"in\":70,\"co\":\"g\"}],\"it\":1}";

    /**
     * Points in the training plan to draw the graph.
     */
    public static Point[] planOne = {new Point(0, 30), new Point(5, 30), new Point(5, 50), new Point(15, 50), new Point(15, 40), new Point(27, 40), new Point(27, 20), new Point(35, 20)};
    public static Point[] planTwo = {new Point(0, 50), new Point(5, 50), new Point(5, 100), new Point(7, 100), new Point(7, 40), new Point(14, 40)};

    /**
     * Duration of the training plans in ms.
     */
    public static int durationPlanOne = 35000;
    public static int durationPlanTwo = 14000;

}
