package fi.iki.murgo.ping;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

public class TcpPinger {

    private static final int PORT = 56667;
    private DataInputStream mInput;
    private DataOutputStream mOutput;
    private boolean mRunning;
    private long mRtt;
    private int mRepliesReceived;
    private Socket mSocket;
    private int mPacketsSent;

    private PingCallback mCallback;
    public void setCallback(PingCallback callback) {
        this.mCallback = callback;
    }

    public void open() {
        mRunning = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mRunning) {
                    listen();
                }
            }
        }).start();
    }

    public void close() {
        mRunning = false;

        try {
            mSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void listen() {
        try {
            mSocket = new Socket();
            mSocket.setKeepAlive(false);
            mSocket.setReceiveBufferSize(9);
            mSocket.setSendBufferSize(9);
            mSocket.setTcpNoDelay(true);
            mSocket.connect(new InetSocketAddress("koti.murgo.iki.fi", PORT));
            mInput = new DataInputStream(mSocket.getInputStream());
            mOutput = new DataOutputStream(mSocket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        while (mRunning) {
            Byte b;
            long nanoTime;

            try {
                b = mInput.readByte();
                nanoTime = mInput.readLong();
            } catch (SocketException e) {
                return;
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            if (b == 'P') {
                // we were pinged, respond
                try {
                    mOutput.writeByte((byte)'R');
                    mOutput.writeLong(nanoTime);
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            } else if (b == 'R') {
                mRtt = System.nanoTime() - nanoTime;
                mRepliesReceived++;
                if (mCallback != null) {
                    mCallback.cb();
                }
            } else {
                // broken packet
                return;
            }
        }
    }

    public void ping() {
        if (mOutput == null) {
            return;
        }

        long nanoTime = System.nanoTime();
        try {
            mOutput.writeByte('P');
            mOutput.writeLong(nanoTime);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mPacketsSent++;
    }

    public String getInfo() {
        return "TCP: " + String.format("%1$,.2f", mRtt / 1e6) + " ms (" + mRepliesReceived + "/" + mPacketsSent + ")";
    }
}
