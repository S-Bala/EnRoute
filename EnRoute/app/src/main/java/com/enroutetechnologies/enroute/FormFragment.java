package com.enroutetechnologies.enroute;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by Sanhar on 2016-11-12.
 */

public class FormFragment extends DialogFragment {
    private EditText mFrom;
    private EditText mTo;
    private EditText mSearch;
    /* The activity that creates an instance of this dialog fragment must
 * implement this interface in order to receive event callbacks.
 * Each method passes the DialogFragment in case the host needs to query it. */
    public interface Listener {
        void onDialogPositiveClick(DialogInterface dialog, EditText text);
        void onDialogNegativeClick(DialogInterface dialog);
        void onFromClick(EditText editText);
        void onToClick(EditText editText);
        void onSearchClick(EditText editText);
    }

    // Use this instance of the interface to deliver action events
    Listener mListener;

//    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (Listener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View layout = inflater.inflate(R.layout.edit_form,null);
        initButtons(layout);
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(layout)
                // Add action buttons
                .setPositiveButton(R.string.search, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onDialogPositiveClick(dialog,mSearch);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onDialogNegativeClick(dialog);
                    }
                });

        return builder.create();
    }

    private void initButtons(View layout){
        mFrom = (EditText)layout.findViewById(R.id.from);
        mTo = (EditText)layout.findViewById(R.id.to);
        mSearch = (EditText)layout.findViewById(R.id.search);

        mFrom.setOnClickListener(onClickListener);
        mTo.setOnClickListener(onClickListener);
        mSearch.setOnClickListener(onClickListener);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            switch(v.getId()){
                case R.id.from:
                    mListener.onFromClick(mFrom);
                    break;
                case R.id.to:
                    mListener.onToClick(mTo);
                    break;
                case R.id.search:
                    mListener.onSearchClick(mSearch);
                    break;
            }
        }
    };
}
