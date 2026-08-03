package com.androidstarter.app.ui.custom;

import android.content.Context;
import android.text.InputType;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * Fluent builder for common Material Dialogs.
 *
 * Usage:
 *   // Confirmation
 *   AppDialog.confirm(context, "Delete?", "This action cannot be undone.", () -> deleteItem());
 *
 *   // Input
 *   AppDialog.input(context, "Rename", "Enter new name", text -> rename(text));
 *
 *   // Info
 *   AppDialog.info(context, "Success", "Your data has been saved.");
 *
 *   // Custom
 *   AppDialog.builder(context)
 *       .title("Choose")
 *       .items(new String[]{"Option A", "Option B"}, (which) -> handle(which))
 *       .show();
 */
public class AppDialog {

    // ---- Quick Shortcuts ----

    /** Show a simple info/alert dialog with OK button. */
    public static void info(Context context, String title, String message) {
        new MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    /** Show a confirmation dialog with Yes/No actions. */
    public static void confirm(Context context, String title, String message, Runnable onConfirm) {
        new MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Yes", (d, w) -> onConfirm.run())
                .setNegativeButton("No", null)
                .show();
    }

    /** Show a confirmation dialog with custom button labels. */
    public static void confirm(Context context, String title, String message,
                               String positiveLabel, String negativeLabel, Runnable onConfirm) {
        new MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveLabel, (d, w) -> onConfirm.run())
                .setNegativeButton(negativeLabel, null)
                .show();
    }

    /** Show a destructive confirmation (red positive button). */
    public static void destructive(Context context, String title, String message, Runnable onConfirm) {
        new MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Delete", (d, w) -> onConfirm.run())
                .setNegativeButton("Cancel", null)
                .show();
    }

    /** Show an input dialog with text field. */
    public static void input(Context context, String title, String hint, OnInputListener listener) {
        input(context, title, hint, "", InputType.TYPE_CLASS_TEXT, listener);
    }

    /** Show an input dialog with pre-filled text and input type. */
    public static void input(Context context, String title, String hint,
                             String prefill, int inputType, OnInputListener listener) {
        EditText editText = new EditText(context);
        editText.setHint(hint);
        editText.setText(prefill);
        editText.setInputType(inputType);
        editText.setSelection(editText.getText().length());

        FrameLayout container = new FrameLayout(context);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int margin = (int) (24 * context.getResources().getDisplayMetrics().density);
        params.setMargins(margin, 0, margin, 0);
        editText.setLayoutParams(params);
        container.addView(editText);

        new MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setView(container)
                .setPositiveButton("OK", (d, w) -> listener.onInput(editText.getText().toString().trim()))
                .setNegativeButton("Cancel", null)
                .show();
    }

    /** Show a single-choice list dialog. */
    public static void singleChoice(Context context, String title, String[] items,
                                    int checkedItem, OnItemSelectedListener listener) {
        new MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setSingleChoiceItems(items, checkedItem, (d, which) -> {
                    listener.onItemSelected(which);
                    d.dismiss();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /** Show a multi-choice list dialog. */
    public static void multiChoice(Context context, String title, String[] items,
                                   boolean[] checkedItems, OnMultiChoiceListener listener) {
        new MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setMultiChoiceItems(items, checkedItems, (d, which, isChecked) ->
                        checkedItems[which] = isChecked)
                .setPositiveButton("OK", (d, w) -> listener.onSelected(checkedItems))
                .setNegativeButton("Cancel", null)
                .show();
    }

    /** Return a new Builder for advanced customization. */
    public static Builder builder(Context context) {
        return new Builder(context);
    }

    // ---- Callbacks ----

    public interface OnInputListener {
        void onInput(String text);
    }

    public interface OnItemSelectedListener {
        void onItemSelected(int index);
    }

    public interface OnMultiChoiceListener {
        void onSelected(boolean[] checkedItems);
    }

    // ---- Fluent Builder ----

    public static class Builder {
        private final MaterialAlertDialogBuilder dialogBuilder;

        Builder(Context context) {
            dialogBuilder = new MaterialAlertDialogBuilder(context);
        }

        public Builder title(String title) {
            dialogBuilder.setTitle(title);
            return this;
        }

        public Builder title(int resId) {
            dialogBuilder.setTitle(resId);
            return this;
        }

        public Builder message(String message) {
            dialogBuilder.setMessage(message);
            return this;
        }

        public Builder message(int resId) {
            dialogBuilder.setMessage(resId);
            return this;
        }

        public Builder positive(String label, Runnable action) {
            dialogBuilder.setPositiveButton(label, (d, w) -> { if (action != null) action.run(); });
            return this;
        }

        public Builder negative(String label, Runnable action) {
            dialogBuilder.setNegativeButton(label, (d, w) -> { if (action != null) action.run(); });
            return this;
        }

        public Builder negative(String label) {
            dialogBuilder.setNegativeButton(label, null);
            return this;
        }

        public Builder items(String[] items, OnItemSelectedListener listener) {
            dialogBuilder.setItems(items, (d, which) -> listener.onItemSelected(which));
            return this;
        }

        public Builder cancelable(boolean cancelable) {
            dialogBuilder.setCancelable(cancelable);
            return this;
        }

        public void show() {
            dialogBuilder.show();
        }
    }
}
