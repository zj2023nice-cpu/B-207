package com.smart.elderly.common;

public final class WorkbenchConstants {

    private WorkbenchConstants() {
    }

    public static final String ITEM_TYPE_WARNING = "WARNING";
    public static final String ITEM_TYPE_NOTIFICATION = "NOTIFICATION";
    public static final String ITEM_TYPE_HEALTH_RECORD = "HEALTH_RECORD";
    public static final String ITEM_TYPE_FOLLOWUP_TASK = "FOLLOWUP_TASK";

    public static final int SCORE_TYPE_WARNING = 100;
    public static final int SCORE_TYPE_FOLLOWUP_TASK = 90;
    public static final int SCORE_TYPE_NOTIFICATION = 70;
    public static final int SCORE_TYPE_HEALTH_RECORD = 50;

    public static final int SCORE_LEVEL_HIGH = 3;
    public static final int SCORE_LEVEL_MEDIUM = 2;
    public static final int SCORE_LEVEL_LOW = 1;

    public static final int PRIORITY_HIGH_THRESHOLD = 120;
    public static final int PRIORITY_MEDIUM_THRESHOLD = 80;

    public static final String ACTION_OPEN = "OPEN";
    public static final String ACTION_READ = "READ";
    public static final String ACTION_VIEW = "VIEW";
    public static final String ACTION_START = "START";
    public static final String ACTION_COMPLETE = "COMPLETE";

    public static final String ACTION_TYPE_PRIMARY = "PRIMARY";
    public static final String ACTION_TYPE_SUCCESS = "SUCCESS";
    public static final String ACTION_TYPE_WARNING = "WARNING";
    public static final String ACTION_TYPE_INFO = "INFO";

    public static final String METHOD_ROUTE = "ROUTE";
    public static final String METHOD_PUT = "PUT";
}
