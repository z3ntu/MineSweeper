package io.github.z3ntu.minesweeper;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Written by Luca Weiss (z3ntu)
 * https://github.com/z3ntu
 */
public class MainActivity extends Activity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private static final String bombMark = "\u2691"; // ⚑
    private static final String unknown = "\u25A1"; // □

    private int numBombs;
    private int cols;
    private int rows;
    private Game game;
    private GridView gridView;
    private TextView textView;

    private boolean gameOver;

    private String[] fields;

    private ArrayAdapter<String> arrayAdapter;

    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
//        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
//        setActionBar(myToolbar);

        setupVars();
        setupGame();
    }

    private void setupVars() {
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        setupPlayingFieldVariables();

        gridView = (GridView) findViewById(R.id.gridview);
        textView = (TextView) findViewById(R.id.textView);

        arrayAdapter = new ArrayAdapter<>(this, R.layout.field_layout, fields);

        gridView.setAdapter(arrayAdapter);
        gridView.setOnItemClickListener(this);
        gridView.setOnItemLongClickListener(this);
        gridView.setNumColumns(cols);

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                restartGame();
            }
        });
    }

    private void setupPlayingFieldVariables() {
        rows = Integer.parseInt(sharedPref.getString("rows", "9"));
        cols = Integer.parseInt(sharedPref.getString("columns", "9"));
        numBombs = Integer.parseInt(sharedPref.getString("bombs", "10"));
        fields = new String[cols * rows];
    }

    private void setupGame() {
        game = new Game(cols, rows, numBombs);

        for (int i = 0; i < fields.length; i++) {
            fields[i] = unknown;
        }
        arrayAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (!gameOver) {
            if (fields[i].equals(bombMark)) {
                Toast.makeText(this, "You can't uncover a flagged field!", Toast.LENGTH_SHORT).show();
                return;
            }
            Game.Result result = game.uncover(iToX(i), iToY(i));
            Game.GameState gameState = game.getGameState(iToX(i), iToY(i));
            if (gameState == Game.GameState.BOMB) {
                textView.setText(R.string.gameover);
                gameOver = true;
            } else if(gameState == Game.GameState.ZERO) {
                updateAllFields();
            }

            fields[i] = gameState.getStr();
            arrayAdapter.notifyDataSetChanged();

            checkIfWon(result);
        } else {
            gameOverWarning();
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (!gameOver) {
            Game.Result result;
            switch (fields[i]) {
                case bombMark:
                    fields[i] = unknown;
                    result = game.flag(iToX(i), iToY(i));
                    break;
                case unknown:
                    fields[i] = bombMark;
                    result = game.flag(iToX(i), iToY(i));
                    break;
                default:
                    result = Game.Result.INVALID;
                    break;
            }
            checkIfWon(result);
            arrayAdapter.notifyDataSetChanged();
        } else {
            gameOverWarning();
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void gameOverWarning() {
        Toast.makeText(this, "You have lost!", Toast.LENGTH_SHORT).show();
    }

    private void updateAllFields() {
        for (int i = 0; i < fields.length; i++) {
            if(game.getShowState(iToX(i), iToY(i)) == Game.ShowState.VISIBLE)
                fields[i] = game.getGameState(iToX(i), iToY(i)).getStr();
        }
        arrayAdapter.notifyDataSetChanged();
    }

    private int iToX(int i) {
        return i % cols;
//        return i / rows;
    }

    private int iToY(int i) {
        return i / rows;
//        return i % cols;
    }

    private void checkIfWon(Game.Result result) {
        if(result == Game.Result.WIN) {
            textView.setText(R.string.won_text);
        }
    }

    private void restartGame() {
        gameOver = false;
        textView.setText("");
        setupGame();
    }
}
