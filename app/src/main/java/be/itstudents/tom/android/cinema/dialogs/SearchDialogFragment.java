package be.itstudents.tom.android.cinema.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import be.itstudents.tom.android.cinema.R;


public class SearchDialogFragment extends DialogFragment {

    private SearchManager searchManager;

    public void setSearchManager(SearchManager searchManager) {
        this.searchManager = searchManager;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.editdialog, null);
        builder.setView(view)
                .setTitle(R.string.searchtxt);
        ImageView image = (ImageView) view.findViewById(R.id.image);
        image.setImageResource(android.R.drawable.ic_menu_search);

        Button btn = (Button) view.findViewById(R.id.searchgo);
        btn.setText(R.string.searchgo);
        btn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {

                EditText text = (EditText) view.findViewById(R.id.text);


                if (text.getText().toString().equals("")) {
                    dismiss();
                    return;
                }

                searchManager.doSearch(text.getText().toString());

                dismiss();
            }
        });
        return builder.create();
    }

    public interface SearchManager {
        void doSearch(String pattern);
    }

}