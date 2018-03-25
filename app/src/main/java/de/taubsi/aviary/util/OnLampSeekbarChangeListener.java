package de.taubsi.aviary.util;

import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.widget.SeekBar;
import android.widget.TextView;
import de.taubsi.aviary.ManualActivity;
import de.taubsi.aviary.R;
import de.taubsi.aviary.service.ManualService;

import java.io.IOException;

public class OnLampSeekbarChangeListener implements SeekBar.OnSeekBarChangeListener {

    private int lampnumber;
    private TextView textView;
    private Context context;

    public OnLampSeekbarChangeListener(int lampnumber, TextView textView, Context context) {
        this.lampnumber = lampnumber;
        this.textView = textView;
        this.context = context;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        textView.setText(context.getString(R.string.percent, progress));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if(((ManualActivity)context).isManual())
            new SetLampTask().execute(lampnumber, seekBar.getProgress());
    }


    public class SetLampTask extends AsyncTask<Integer, Void, String> {

        @Override
        protected String doInBackground(Integer... params) {
            android.util.Log.v("SetLampTask", params[0].toString() + " - " + params[1].toString());
            try {
                return new ManualService(context).setLamp(params[0], params[1]);
            } catch (IOException e) {
                return e.getLocalizedMessage();
            }
        }

        @Override
        protected void onPostExecute(final String s) {
            super.onPostExecute(s);
            if(s!=null){
                ((ManualActivity)context).showSettingsAlert(s);
            }
        }
    }
}
