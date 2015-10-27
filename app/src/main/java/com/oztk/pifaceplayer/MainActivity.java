package com.oztk.pifaceplayer;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements TcpServer.TcpCallback,
        CompoundButton.OnCheckedChangeListener,
        MediaRecorder.OnInfoListener,
        MediaPlayer.OnCompletionListener, View.OnClickListener {
    private static final String TAG = "com.oztk.pifaceplayer.MainActivity";
    private static final int MAX_RETRIES = 3;
    private static final String OUTPUT_BASE_FILENAME = "halloween_voice";
    private static final String OUTPUT_FILE_EXTENSION = ".3gp";
    private static final int SOUND_MAX_DURATION = 10000;

    private TcpServer server;
    private int connectionRetries = 0;
    private MediaRecorder recorder;
    private MediaPlayer player;
    private PowerManager.WakeLock wakeLock;

    private ToggleButton mBtnRecord1;
    private ImageButton mBtnPlay1;
    private ToggleButton mBtnRecord2;
    private ImageButton mBtnPlay2;
    private ToggleButton mBtnRecord3;
    private ImageButton mBtnPlay3;
    private ToggleButton mBtnRecord4;
    private ImageButton mBtnPlay4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        server = new TcpServer(6789);

        mBtnRecord1 = (ToggleButton) findViewById(R.id.activity_main_btn_recording1);
        mBtnPlay1 = (ImageButton) findViewById(R.id.activity_main_btn_play1);
        mBtnRecord2 = (ToggleButton) findViewById(R.id.activity_main_btn_recording2);
        mBtnPlay2 = (ImageButton) findViewById(R.id.activity_main_btn_play2);
        mBtnRecord3 = (ToggleButton) findViewById(R.id.activity_main_btn_recording3);
        mBtnPlay3 = (ImageButton) findViewById(R.id.activity_main_btn_play3);
        mBtnRecord4 = (ToggleButton) findViewById(R.id.activity_main_btn_recording4);
        mBtnPlay4 = (ImageButton) findViewById(R.id.activity_main_btn_play4);

        mBtnRecord1.setOnCheckedChangeListener(this);
        mBtnPlay1.setOnClickListener(this);
        mBtnRecord2.setOnCheckedChangeListener(this);
        mBtnPlay2.setOnClickListener(this);
        mBtnRecord3.setOnCheckedChangeListener(this);
        mBtnPlay3.setOnClickListener(this);
        mBtnRecord4.setOnCheckedChangeListener(this);
        mBtnPlay4.setOnClickListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        server.stop();
//        wakeLock.release();
    }

    @Override
    protected void onResume() {
        super.onResume();

//        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
//        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
//        wakeLock.acquire();

        server.start(this);
    }

    @Override
    public void receive(String data) {
        Toast.makeText(this, String.format("Playing sound %s", data), Toast.LENGTH_LONG).show();
        String error = null;
        try {
            startPlaying(Integer.parseInt(data));
        } catch (IOException e) {
            error = "Error reading the sound file";
        } catch (NumberFormatException e) {
            error = "Malformed data received from Pi";
        } finally {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void faulted() {
        if (connectionRetries < MAX_RETRIES) {
            server.start(this);
        }
        connectionRetries++;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        try {
            if (!isChecked) {
                stopRecording();
            } else {
                int index = Integer.parseInt(buttonView.getTag().toString());
                startRecording(index);
            }
        } catch (IOException e) {
            recorder = null;
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        } catch (RuntimeException e) {
            Toast.makeText(this, "Stop failed", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onInfo(MediaRecorder mr, int what, int extra) {
        if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
            stopRecording();
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        stopPlaying();
    }

    @Override
    public void onClick(View v) {
        if (player != null) {
            return;
        }

        int index = Integer.parseInt(v.getTag().toString());
        try {
            startPlaying(index);
        } catch (IOException e) {
            player = null;
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void startRecording(int soundIndex) throws IOException {
        if (recorder != null) {
            return;
        }

        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);

        String filePath = buildOutputFilePath(soundIndex);
        File outputFile = new File(filePath);
        if (outputFile.exists() && !outputFile.delete()) {
            throw new IOException("Unable to overwrite output file");
        }

        recorder.setOutputFile(filePath);

        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.HE_AAC);
        recorder.setMaxDuration(SOUND_MAX_DURATION);
        recorder.setOnInfoListener(this);

        recorder.prepare();
        recorder.start();
    }

    private void stopRecording() {
        if (recorder == null) {
            return;
        }

        recorder.stop();
        recorder.release();
        recorder = null;
    }

    private void startPlaying(int soundIndex) throws IOException {
        if (player != null) {
            return;
        }

        player = new MediaPlayer();
        player.setDataSource(buildOutputFilePath(soundIndex));
        player.setOnCompletionListener(this);
        player.prepare();
        player.start();
    }

    private void stopPlaying() {
        if (player == null) {
            return;
        }

        player.stop();
        player.release();
        player = null;
    }

    private String buildOutputFilePath(int soundIndex) {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_NOTIFICATIONS).getAbsolutePath()
                + OUTPUT_BASE_FILENAME
                + soundIndex
                + OUTPUT_FILE_EXTENSION;
    }
}
