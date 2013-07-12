package fi.iki.murgo.ping;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class UdpPinger implements Runnable {
    private static final int PORT = 56666;
    private DatagramSocket mSocket;
    private final Object mSocketLock = new Object();

    private byte[] mMsgBuf = new byte[9];
    private volatile boolean mRunning = false;
    private PingCallback mCallback;

    private long mRtt;
    private int mRepliesReceived;
    private int mPacketsSent;

    public void setCallback(PingCallback callback) {
        this.mCallback = callback;
    }

    public void open() {
        mRunning = true;
        Thread t = new Thread(this);
        t.start();
    }

    public void close() {
        mRunning = false;
        if (mSocket != null) {
            mSocket.close();
        }
    }

    public void ping() {
        mMsgBuf[0] = 'P';

        long nanoTime = System.nanoTime();
        ByteBuffer.wrap(mMsgBuf).putLong(1, nanoTime);

        mPacketsSent++;

        try {
            sendUdp(mMsgBuf);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendUdp(byte[] data) throws IOException {
        DatagramPacket packet = new DatagramPacket(data, data.length);

        synchronized (mSocketLock) {
            mSocket.send(packet);
        }
    }

    @Override
    public void run() {
        InetAddress mAddress;
        try {
            mAddress = InetAddress.getAllByName("koti.murgo.iki.fi")[0];
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return;
        }

        try {
            mSocket = new DatagramSocket(PORT);
            mSocket.connect(mAddress, PORT);
        } catch (SocketException e) {
            e.printStackTrace();
            return;
        }

        DatagramPacket mReceivePacket = new DatagramPacket(new byte[9], 9);
        while (mRunning) {
            try {
                mSocket.receive(mReceivePacket);
            } catch (SocketException e) {
                System.out.println("Socket closed");
                return;
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            byte[] buf = mReceivePacket.getData();
            if (buf[0] == 'P') {
                // we were pinged, respond
                buf[0] = 'R';

                try {
                    sendUdp(buf);
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            } else if (buf[0] == 'R') {
                long nanoTime = ByteBuffer.wrap(buf).getLong(1);

                mRtt = System.nanoTime() - nanoTime;
                mRepliesReceived++;
                if (mCallback != null) {
                    mCallback.cb();
                }
            }
        }
    }

    public String getInfo() {
        return "UDP: " + String.format("%1$,.2f", mRtt / 1e6) + " ms (" + mRepliesReceived + "/" + mPacketsSent + ")";
    }
}
