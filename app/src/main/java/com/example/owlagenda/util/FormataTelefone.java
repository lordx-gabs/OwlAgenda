package com.example.owlagenda.util;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

public class FormataTelefone implements TextWatcher {
    private EditText editText;
    private boolean isFormatting = false;
    private boolean deletedDigit = false;

    public FormataTelefone(EditText editText) {
        this.editText = editText;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        deletedDigit = count > after;
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable editable) {
        if (isFormatting) {
            return;
        }

        isFormatting = true;

        String digits = editable.toString().replaceAll("[^\\d]", "");
        StringBuilder formatted = new StringBuilder();

        if (digits.length() == 1) {
            formatted.append("(").append(digits);
        } else if (digits.length() == 2) {
            formatted.append("(").append(digits);
        } else if (digits.length() >= 3 && digits.length() <= 6) {
            formatted.append("(").append(digits, 0, 2).append(") ").append(digits, 2, digits.length());
        } else if (digits.length() >= 7 && digits.length() <= 10) {
            formatted.append("(").append(digits, 0, 2).append(") ").append(digits, 2, 6).append("-").append(digits, 6, digits.length());
        } else if (digits.length() == 11) {
            formatted.append("(").append(digits, 0, 2).append(") ").append(digits, 2, 3).append(" ").append(digits, 3, 7).append("-").append(digits, 7, digits.length());
        } else if (digits.length() > 11) {
            formatted.append("(").append(digits, 0, 2).append(") ").append(digits, 2, 3).append(" ").append(digits, 3, 7).append("-").append(digits, 7, 11);
        }

        editText.setText(formatted.toString());
        editText.setSelection(Math.min(formatted.length(), 16));

        isFormatting = false;
    }
}
