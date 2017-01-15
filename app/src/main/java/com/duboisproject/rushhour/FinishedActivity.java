package com.duboisproject.rushhour;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.duboisproject.rushhour.R;

import java.util.ArrayList;

/**
 * The user has finished the game / wants to see the highscores
 * @author Camil Staps
 */
public class FinishedActivity extends ActionBarActivity {

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finished);

        context = this;

        final HighScoreList list = new HighScoreList(this);

        // If there is a score, we add a new highscore (user can enter name)
        final int score = getIntent().getIntExtra("score", -1);
        if (score != -1) {
            TheSoundPool.getSoundPool(getBaseContext()).play(TheSoundPool.soundVictoryId, 1, 1, 1, 0, 1);
            final EditText input = new EditText(this);
            new AlertDialog.Builder(this)
                    .setTitle("Congratulations!")
                    .setMessage("Enter name:")
                    .setView(input)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            Editable value = input.getText();
                            list.addToList(new HighScore(score, value.toString()));
                            list.save(context);
                        }
                    })
                    .show();
        }

        // Populate listview with highscores
        ListView highscoresListView = (ListView) findViewById(R.id.highscoresListView);
        HighScoreAdapter arrayAdapter = new HighScoreAdapter(this, R.layout.highscore_item, list.getList());
        highscoresListView.setAdapter(arrayAdapter);
    }

    @Override
    public void onBackPressed() {
        // See http://stackoverflow.com/a/13483049/1544337
        Intent intent = new Intent();
        if (getParent() == null) {
            setResult(Activity.RESULT_OK, intent);
        } else {
            getParent().setResult(Activity.RESULT_OK, intent);
        }
        finish();
    }

    private class HighScoreAdapter extends ArrayAdapter<HighScore> {
        Context context;
        int layoutResourceId;
        ArrayList<HighScore> data;

        public HighScoreAdapter(Context context, int layoutResourceId, ArrayList<HighScore> data) {
            super(context, layoutResourceId, data);
            this.layoutResourceId = layoutResourceId;
            this.context = context;
            this.data = data;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            HighScoreHolder holder;

            if(row == null) {
                LayoutInflater inflater = ((Activity)context).getLayoutInflater();
                row = inflater.inflate(layoutResourceId, parent, false);

                holder = new HighScoreHolder();
                holder.score = (TextView)row.findViewById(R.id.score);
                holder.name = (TextView)row.findViewById(R.id.name);

                row.setTag(holder);
            } else {
                holder = (HighScoreHolder)row.getTag();
            }

            HighScore highscore = data.get(position);
            holder.name.setText(highscore.getName());
            holder.score.setText(Integer.toString(highscore.getScore()));

            return row;
        }

        private class HighScoreHolder {
            TextView score, name;
        }
    }

}
