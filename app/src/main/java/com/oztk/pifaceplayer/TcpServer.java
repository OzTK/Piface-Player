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
    private ServerSocket socket;
    private TcpCallback callback;
    private boolean stop;

    public TcpServer(int port) {
        this.port = port;
    }

    public void start(@NonNull TcpCallback callback) {
        this.callback = callback;
        try {
            socket = new ServerSocket(port);
            serverThread = new Thread(this);
            serverThread.start();
        } catch (IOException e) {
            Log.e(TAG, "Failed to instantiate socket: " + e.getMessage());
            if (this.callback != null) {
                callback.faulted();
            }
        }
    }

    public void stop() {
        try {
            stop = true;
            socket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close socket: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
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
            if (callback != null) {
                callback.faulted();
            }
        }
    }

    public interface TcpCallback {
        void receive(String data);
        void faulted();
    }
}
