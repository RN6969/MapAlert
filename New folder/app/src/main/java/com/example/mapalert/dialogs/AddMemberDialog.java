package com.example.mapalert.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.example.mapalert.R;

public class AddMemberDialog extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_add_member);

        EditText usernameField = dialog.findViewById(R.id.usernameInput);
        EditText nicknameField = dialog.findViewById(R.id.nicknameInput);
        Button submitButton = dialog.findViewById(R.id.submitMemberBtn);

        submitButton.setOnClickListener(v -> {
            String username = usernameField.getText().toString();
            String nickname = nicknameField.getText().toString();
            if (!username.isEmpty()) {
                // Save member logic
                dismiss();
            }
        });

        return dialog;
    }
}
