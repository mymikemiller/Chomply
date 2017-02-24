package com.mikemiller.chomply.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.mikemiller.chomply.Constants;
import com.mikemiller.chomply.R;
import com.mikemiller.chomply.Constants.Info;
import com.mikemiller.chomply.R.string;

public class SortOptionDialogFragment extends DialogFragment {
	
	private Info mSelectedItem = Info.CALORIES;
	
	private static final Info[] SORT_ON_LIST = { Info.CALORIES, Info.CHOMPSCORE, Info.NET_CARBS,
		Info.CARBS, Info.FAT, Info.SATURATED_FAT, Info.CHOLESTEROL, Info.SODIUM, Info.PROTEIN, Info.FIBER, Info.ITEM_NAME};

	
	public interface SortOptionsDialogListener {
        public void onSortOptionSelected(DialogFragment dialog, Info selectedSortOption);
    }
	
	// Use this instance of the interface to deliver action events
	SortOptionsDialogListener mListener;
	
	// Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (SortOptionsDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement SortOptionsDialogListener");
        }
    }
	
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

	    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	    
	    String[] sortOnListString = new String[SORT_ON_LIST.length];
		for (int i = 0; i < SORT_ON_LIST.length; ++i) {
			sortOnListString[i] = SORT_ON_LIST[i].readableName;
		}
		
		int selectedIndex = getIndexToSelect(mSelectedItem);
	    
	    // Set the dialog title
	    builder.setTitle(R.string.sort_dialog_prompt)
	    // Specify the list array, the items to be selected by default (null for none),
	    // and the listener through which to receive callbacks when items are selected
	    	.setSingleChoiceItems(sortOnListString, selectedIndex, 
	    			new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							mSelectedItem = SORT_ON_LIST[which];
							mListener.onSortOptionSelected(SortOptionDialogFragment.this, mSelectedItem);
							dialog.dismiss();
						}
						
					});
					/*
	    // Set the action buttons
	           .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
	               @Override
	               public void onClick(DialogInterface dialog, int id) {
	                   // User clicked OK, so save the mSelectedItems results somewhere
	                   // or return them to the component that opened the dialog
	                   ...
	               }
	           })
	           .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
	               @Override
	               public void onClick(DialogInterface dialog, int id) {
	                   ...
	               }
	           });
	*/
	    return builder.create();
    }
    
    private int getIndexToSelect(Info sortOn) {
    	// Get the selected Info's index into the array
		int selectedIndex = 0;
		for (int i = 0; i < SORT_ON_LIST.length; ++i) {
			if (mSelectedItem == SORT_ON_LIST[i]) {
				selectedIndex = i;
				break;
			}
		}
		return selectedIndex;
    }
    
    // This must be set before showing the dialog
    public void setSelectedSortOption(Info sortOn) {
    	mSelectedItem = sortOn;
    }
}