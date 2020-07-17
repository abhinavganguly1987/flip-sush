package com.toto.sush.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.toto.sush.R;

public class QuickResponseDialogFragment extends DialogFragment {

    private String LOG_TAG = "^^^[TOTO] QuickResponseDialogFragment";


    static int preselectedIndex;

    // Use this instance of the interface to deliver action events
    QuickResponseDialogListener listener;

    public QuickResponseDialogFragment() {
    }


    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the listIndex of the chosen quick SMS item. */
    public interface QuickResponseDialogListener {
        void onDialogPositiveClick(int selectedListIndex);
    }

    public static QuickResponseDialogFragment newInstance(int selected) {
        preselectedIndex = selected;
        QuickResponseDialogFragment quickResponseDialogFragment = new QuickResponseDialogFragment();
        return quickResponseDialogFragment;
    }

//    @Nullable
//    @Override
//    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
//        return inflater.inflate(R.layout.quick_response_dialog_fragment, container);
//    }


    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            //Instantiate QuickResponseDialogListener so that we can send events to the host
            listener = (QuickResponseDialogListener) context;

        } catch (ClassCastException e) {
            //then activity doesn't implement the interface, throw an exception
            throw new ClassCastException(getActivity().toString() + " must implement QuickResponseDialogListener !!");

        }

    }


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.set_auto_sms)
                .setSingleChoiceItems(R.array.quickSMSList, preselectedIndex, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.i(LOG_TAG, " in QuickResponseDialogFragment.on Selecting An Item, new selected Index is " + i);

                        preselectedIndex = i;
                    }
                })
                .setPositiveButton(R.string.set, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Log.i(LOG_TAG, " in QuickResponseDialogFragment. on selecting SET button, new selected Index is " + preselectedIndex);

                        dialog.dismiss();
                        listener.onDialogPositiveClick(preselectedIndex);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Log.i(LOG_TAG, " in QuickResponseDialogFragment. on selecting CANCEL button, existing selected Index is " + preselectedIndex);

                        dialog.cancel();
                    }
                });
        // Create the AlertDialog object and return it
        AlertDialog qrsmsDialog = builder.create();
        qrsmsDialog.setCanceledOnTouchOutside(false);
        return qrsmsDialog;
    }

}
