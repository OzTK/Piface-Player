package com.oztk.pifaceplayer;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author Paul Duguet
 * @version 1
 */
public class TcpServer implements Runnable {
    private static final String TAG = "TcpServer" ;

    private int port;
    private Thread serverThread;
    private boolean stop;
    private TcpCallback callback;

    public TcpServer(int port) {
        this.port = port;
    }

    public void start(@NonNull TcpCallback callback) {
        this.callback = callback;
        serverThread = new Thread(this);
        serverThread.start();
    }

    public void stop() {
        stop = true;
    }

    @Override
    public void run() {
        try {
            ServerSocket socket = new ServerSocket(port);

            while (!stop) {
                Socket connectionSocket = socket.accept();
                BufferedReader requestReader = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));

                final String requestData = requestReader.readLine();

                if (callback != null) {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.receive(requestData);
                        }
                    });
                }

                requestReader.close();
            }

            callback = null;
            serverThread = null;
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public interface TcpCallback {
        void receive(String data);
    }
}
