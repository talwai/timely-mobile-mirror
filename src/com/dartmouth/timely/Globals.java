package com.dartmouth.timely;



// All the global constants are put here
public abstract class Globals {
	
	// notification text
	public static final String LEAVE_FOR_CLASS_TEXT = "Time to leave for class";
	
    public static final int NO_DATA_FOUND = -1;


	// keys for updateBar
	public static final int VIBRATE_PHONE = 1;
	public static final String LIBRARY_VIBRATE = "Phone set to vibrate (in library)";
	
	public static final int UNSILENCE_PHONE = 2;
	public static final String UNSILENCE_PHONE_TEXT = "Phone unsilenced (out of class)";
	
	public static final int LOAD_ESTIMATE = 3;
	
	public static final int LOAD_LUNCH_OPTIONS = 4;
	public static final String LOAD_LUNCH_TEXT= "Dining Menus";
	
	public static final int SCHEDULE_EVENT = 5;
	
	public static final int FOCO_MENU = 6;
	public static final String FOCO_TEXT = "FoCo Dining Menu";
	
	public static final int FOCO_MENU_LOAD = 7;
	
	public static final int KAF_MENU = 8;
	public static final String KAF_TEXT = "King Arthur's Cafe Dining Menu";
	
	public static final int HOP_MENU = 9;
	public static final String HOP_TEXT = "Hop's Dining Menu";
	
	public static final int BOLOCO_MENU = 10;
	public static final String BOLOCO_TEXT = "Boloco's Dining Menu";
	
    public static final String TIME_USAGE_TEXT = "Time Usage";
    public static final String NOT_ENOUGH_DATA_TEXT = "NOT ENOUGH DATA";


	// Constants for sensor services
	public static int GPS_CACHE_SIZE=100;
	public static int ACC_CACHE_SIZE=64;
	
	// common parameters
		public static final long POINT_RADIUS = 100; // in Meters
		public static final long PROX_ALERT_EXPIRATION = -1;
		public static final String PROX_ALERT_INTENT ="com.dartmouth.timely.ProximityAlert";

		// key for geofencing
		public static final String PROX_TYPE_INDIC= "prox";
		public static final int PROX_EVENT_MARKERS = 2;
		public static final int PROX_LUNCH = 1;

}
