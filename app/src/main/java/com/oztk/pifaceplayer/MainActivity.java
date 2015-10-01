package com.oztk.pifaceplayer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements TcpServer.TcpCallback {
    private static final int MAX_RETRIES = 3;

    private TcpServer server;
    private int connectionRetries = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        server = new TcpServer(6789);
        server.start(this);
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
}
