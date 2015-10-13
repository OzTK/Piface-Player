package com.oztk.pifaceplayer;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
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
    private static final int MAX_RETRIES = 3;
    private static final String OUTPUT_BASE_FILENAME = "halloween_voice";
    private static final String OUTPUT_FILE_EXTENSION = ".3gp";
    private static final int SOUND_MAX_DURATION = 10000;

    private TcpServer server;
    private int connectionRetries = 0;
    private MediaRecorder recorder;
    private MediaPlayer player;

    private ToggleButton mBtnRecord1;
    private ImageButton mBtnPlay1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        server = new TcpServer(6789);
        server.start(this);

        mBtnRecord1 = (ToggleButton) findViewById(R.id.activity_main_btn_recording1);
        mBtnPlay1 = (ImageButton) findViewById(R.id.activity_main_btn_play1);

        mBtnRecord1.setOnCheckedChangeListener(this);
        mBtnPlay1.setOnClickListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        server.stop();
    }

    @Override
    public void receive(String data) {
        Toast.makeText(this, data, Toast.LENGTH_LONG).show();
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
        if (!isChecked) {
            stopRecording();
        } else {
            int index = Integer.parseInt(buttonView.getTag().toString());
            try {
                startRecording(index);
            } catch (IOException e) {
                recorder = null;
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
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
