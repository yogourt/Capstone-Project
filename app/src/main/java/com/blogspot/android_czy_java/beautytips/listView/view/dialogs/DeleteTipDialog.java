package com.blogspot.android_czy_java.beautytips.listView.view.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import com.blogspot.android_czy_java.beautytips.R;
import com.blogspot.android_czy_java.beautytips.listView.firebase.FirebaseHelper;

public class DeleteTipDialog extends DialogFragment {

    private String mTipId;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.DialogStyle);
        builder.setIcon(R.drawable.ic_soap);
        builder.setTitle(R.string.delete_dialog_title);
        builder.setMessage(R.string.delete_dialog_message);

        builder.setPositiveButton(R.string.delete_dialog_pos_button,
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                FirebaseHelper.deleteTipWithId(mTipId);
            }
        });

        builder.setNegativeButton(R.string.delete_dialog_neg_button, null);
        return builder.create();
    }

    public void setTipId(String tipId) {
        mTipId = tipId;
    }
}