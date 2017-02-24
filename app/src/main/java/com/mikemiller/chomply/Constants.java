package com.mikemiller.chomply;

import com.mikemiller.chomply.helper.UnknownInfoException;

public class Constants {

	public static final String TAG_APPLICATION_ID = "appId";
	public static final String TAG_APPLICATION_KEY = "appKey";

	public static final String VALUE_APPLICATION_KEY = "e3fa49734b9ffd028ecfa5addb89a58b";
	public static final String VALUE_APPLICATION_ID = "8af503d9";
	
    public enum SortOrder { NONE, ASCENDING, DESCENDING }
	
	public enum Info {
	    BRAND_ID("Brand ID", String.class, "brand_id", "", SortOrder.NONE),
	    BRAND_NAME("Brand Name", String.class, "brand_name", "", SortOrder.NONE),
	    ITEM_NAME("Item Name", String.class, "item_name", "itemName", SortOrder.ASCENDING),
	    ITEM_ID("Item ID", String.class, "_id", "", SortOrder.NONE),
	    ITEM_DESCRIPTION("Description", String.class, "item_description", "", SortOrder.NONE),
	    UPDATED_AT("Updated at", String.class, "updated_at", "", SortOrder.NONE),
	    SERVING_SIZE_QTY("Serving Size", double.class, "nf_serving_size_qty", "valueServingSize", SortOrder.NONE),
	    SERVING_SIZE_UNIT("Serving Size Unit", String.class, "nf_serving_size_unit", "valueServingSizeUnit", SortOrder.NONE),	    
	    CALORIES("Calories", double.class, "nf_calories", "valueCalories", SortOrder.ASCENDING),
	    CARBS("Carbs", double.class, "nf_total_carbohydrate", "valueTotalCarb", SortOrder.ASCENDING),
	    FAT("Fat", double.class, "nf_total_fat", "valueTotalFat", SortOrder.ASCENDING),
	    SATURATED_FAT("Saturated Fat", double.class, "nf_saturated_fat", "valueSatFat", SortOrder.ASCENDING),
	    CONTAINS_GLUTEN("Contains Gluten", boolean.class, "allergen_contains_gluten", "", SortOrder.NONE),
	    FAT_CALS("Calories from fat", double.class, "nf_calories_from_fat", "valueFatCalories", SortOrder.ASCENDING),
	    TRANS_FAT("Trans Fat", double.class, "nf_trans_fatty_acid", "valueTransFat", SortOrder.ASCENDING),
	    POLYUNSATURATED_FAT("Polyunsaturated fat", double.class, "nf_polyunsaturated_fat", "valuePolyFat", SortOrder.ASCENDING),
	    MONOUNSATURATED_FAT("Monounsaturated fat", double.class, "nf_monounsaturated_fat", "valueMonoFat", SortOrder.ASCENDING),
	    CHOLESTEROL("Cholesterol", double.class, "nf_cholesterol", "valueCholesterol", SortOrder.ASCENDING),
	    SODIUM("Sodium", double.class, "nf_sodium", "valueSodium", SortOrder.ASCENDING),
	    FIBER("Fiber", double.class, "nf_dietary_fiber", "valueFibers", SortOrder.DESCENDING),
	    SUGARS("Sugar", double.class, "nf_sugars", "valueSugars", SortOrder.ASCENDING),
	    PROTEIN("Protein", double.class, "nf_protein", "valueProtein", SortOrder.DESCENDING),
	    VITAMIN_A("Vitamin A", double.class, "nf_vitamin_a_dv", "valueVitaminA", SortOrder.DESCENDING),
	    VITAMIN_C("Vitamin C", double.class, "nf_vitamin_c_dv", "valueVitaminC", SortOrder.DESCENDING),
	    CALCIUM("Calcium", double.class, "nf_calcium_dv", "valueCalcium", SortOrder.DESCENDING),
	    IRON("Iron", double.class, "nf_iron_dv", "valueIron", SortOrder.DESCENDING),
	    // note: when adding something here, don't forget to update Item
	    
	    CHOMPSCORE("ChompScore", double.class, "", "", SortOrder.ASCENDING),
	    NET_CARBS("Net Carbs", double.class, "", "", SortOrder.ASCENDING);
		
		public String readableName;
		public Class<?> dataType;
		public String databaseName;
		public String nutritionalLabelName;
		public SortOrder sortOrder;
		private Info(String readableName, Class<?> dataType, String databaseName, String nutritionalLabelName, SortOrder sortOrder) { 
			this.readableName = readableName;
			this.dataType = dataType;
			this.databaseName = databaseName;
			this.nutritionalLabelName = nutritionalLabelName;
			this.sortOrder = sortOrder;
		}
		
		public static Info getInfoFromDatabaseName(String databaseName) throws UnknownInfoException {
			for(Info i : Info.values()) {
				if (i.databaseName.equals(databaseName)) {
					return i;
				}
			}
			// Unknown databaseName
			throw new UnknownInfoException("Unknown databaseName: " + databaseName);
		}
	}
}
