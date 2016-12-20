package xyz.z3ntu.minesweeper;

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

    private boolean shouldInitFields = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        setupVars();
        setupGame();
//        setupDatabase();
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
        if (fields == null) {
            fields = new String[cols * rows];
        } else {
            shouldInitFields = false;
        }
    }

    private void setupGame() {
        game = new Game(cols, rows, numBombs);
        if (shouldInitFields) {
            for (int i = 0; i < fields.length; i++) {
                fields[i] = unknown;
            }
        }
        arrayAdapter.notifyDataSetChanged();
    }

    /*private void setupDatabase() {
        // In any activity just pass the context and use the singleton method
        MinesweeperDatabase helper = MinesweeperDatabase.getInstance(this);
    }*/

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
            } else if (gameState == Game.GameState.ZERO) {
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
            if (game.getShowState(iToX(i), iToY(i)) == Game.ShowState.VISIBLE)
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        System.out.println("---ON SAVE INSTANCE STATE---");
        MinesweeperDatabase db = MinesweeperDatabase.getInstance(this);
        long timestamp = System.currentTimeMillis() / 1000;
        for (int i = 0; i < cols * rows; i++) {
            int x = iToX(i);
            int y = iToY(i);
            db.insertField(i, game.getShowState(x, y), game.getGameState(x, y), timestamp);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        System.out.println("---ON RESTORE INSTANCE STATE---");
        MinesweeperDatabase db = MinesweeperDatabase.getInstance(this);
        Game.ShowState[][] showStates = new Game.ShowState[cols][rows]; // TODO: cols&rows correct?
        Game.GameState[][] gameStates = new Game.GameState[cols][rows];
        int lasttimestamp = db.getLastTimestamp();
        for (int i = 0; i < cols * rows; i++) {
            int x = iToX(i);
            int y = iToY(i);
            Object[] ret = db.getField(i, lasttimestamp);
            gameStates[x][y] = (Game.GameState) ret[0];
            showStates[x][y] = (Game.ShowState) ret[1];
            if (showStates[x][y] == Game.ShowState.VISIBLE) {
                fields[i] = gameStates[x][y].getStr();
            } else {
                fields[i] = unknown;
            }

        }
        game.setGameState(gameStates);
        game.setShowState(showStates);

        arrayAdapter.notifyDataSetChanged();
    }

    private void checkIfWon(Game.Result result) {
        if (result == Game.Result.WIN) {
            textView.setText(R.string.won_text);
        }
    }

    private void restartGame() {
        gameOver = false;
        textView.setText("");
        setupGame();
    }
}
