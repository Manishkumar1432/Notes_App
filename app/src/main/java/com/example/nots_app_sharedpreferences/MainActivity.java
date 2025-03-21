package com.example.nots_app_sharedpreferences;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private EditText editText, searchText;
    private Button addButton;
    private ListView listView;
    private ArrayList<String> notesList, filteredList;
    private ArrayAdapter<String> adapter;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);  //USE FOR CHANGE STATUSBAR COLLOR


        editText = findViewById(R.id.editText);
        searchText = findViewById(R.id.searchText);
        addButton = findViewById(R.id.addButton);
        listView = findViewById(R.id.listView);

        sharedPreferences = getSharedPreferences("NotesApp", MODE_PRIVATE);
        notesList = loadNotes();
        filteredList = new ArrayList<>(notesList);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, filteredList);
        listView.setAdapter(adapter);

        addButton.setOnClickListener(view -> {
            String note = editText.getText().toString().trim();
            if (!note.isEmpty()) {
                notesList.add(note);
                updateList();
                saveNotes();
                editText.setText("");
            }
        });

        listView.setOnItemClickListener((adapterView, view, position, id) -> editNote(position));

        listView.setOnItemLongClickListener((adapterView, view, position, id) -> {
            notesList.remove(position);
            updateList();
            saveNotes();
            return true;
        });

        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                filterNotes(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
    }

    private void saveNotes() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        JSONArray jsonArray = new JSONArray(notesList);
        editor.putString("notes", jsonArray.toString());
        editor.apply();
    }

    private ArrayList<String> loadNotes() {
        ArrayList<String> list = new ArrayList<>();
        String json = sharedPreferences.getString("notes", "[]");
        try {
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                list.add(jsonArray.getString(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    private void editNote(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Note");

        final EditText input = new EditText(this);
        input.setText(notesList.get(position));
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String editedNote = input.getText().toString().trim();
            if (!editedNote.isEmpty()) {
                notesList.set(position, editedNote);
                updateList();
                saveNotes();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void filterNotes(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(notesList);
        } else {
            for (String note : notesList) {
                if (note.toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(note);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void updateList() {
        filteredList.clear();
        filteredList.addAll(notesList);
        adapter.notifyDataSetChanged();
    }
}
