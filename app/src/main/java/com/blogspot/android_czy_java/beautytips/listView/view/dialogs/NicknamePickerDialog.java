package com.blogspot.android_czy_java.beautytips.listView.view.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.blogspot.android_czy_java.beautytips.R;
import com.google.firebase.auth.FirebaseAuth;

import timber.log.Timber;

public class NicknamePickerDialog extends DialogFragment {

    public interface NicknamePickerDialogListener {
        void onDialogSaveButtonClick(String nickname);
    }

    private NicknamePickerDialogListener mNicknamePickerDialogListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.DialogStyle);

        builder.setView(R.layout.dialog_nickname_picker);

        builder.setTitle(R.string.dialog_nickname_title);
        builder.setMessage(R.string.dialog_nickname_message);
        builder.setIcon(R.drawable.ic_soap);
        builder.setPositiveButton(R.string.dialog_button_positive_label,
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        return builder.create();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mNicknamePickerDialogListener = (NicknamePickerDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement NicknamePickerDialog" +
                    ".NicknamePickerDialogListener");
        }

    }

    @Override
    public void onStart() {
        super.onStart();

        EditText nicknameEt = getDialog().findViewById(R.id.nickname_edit_text);
        String displayName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        nicknameEt.setText(displayName);

    }

    @Override
    public void onResume() {
        super.onResume();


        final AlertDialog dialog = (AlertDialog)getDialog();

        /*
        Prevent dialog from closing when clicking on activity in background - this is done because
        we need user to choose nickname at the beginning
        */
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        /*
        We also prevent dialog from closin when edit text is empty
         */
        dialog.getButton(Dialog.BUTTON_POSITIVE).setOnClickListener
                (new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        EditText nicknameEt = getDialog().findViewById(R.id.nickname_edit_text);
                        String nickname = nicknameEt.getText().toString().trim();
                        Timber.d("Chosen nickname: " + nickname);
                        if(TextUtils.isEmpty(nickname)) {
                            Timber.d("Nickname is empty");
                            getDialog().findViewById(R.id.error_text_view).setVisibility(View.VISIBLE);
                        } else {
                            mNicknamePickerDialogListener.onDialogSaveButtonClick(nicknameEt.getText().toString());
                            dialog.dismiss();

                        }
                    }
                });


    }
}