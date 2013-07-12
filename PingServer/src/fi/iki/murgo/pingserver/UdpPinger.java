package fi.iki.murgo.pingserver;

import java.io.IOException;
import java.net.*;

public class UdpPinger implements Runnable {
    private static final int PORT = 56666;
    private DatagramSocket mSocket;
    private final Object mSocketLock = new Object();

    private volatile boolean mRunning = false;
    private DatagramPacket mReceivePacket;

    public boolean open() {
        mReceivePacket = new DatagramPacket(new byte[9], 9);

        try {
            mRunning = true;
            mSocket = new DatagramSocket(PORT);
            Thread t = new Thread(this);
            t.start();
        } catch (SocketException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void close() {
        mRunning = false;
        if (mSocket != null) {
            mSocket.close();
        }
    }

    public void sendUdp(byte[] data, SocketAddress address) throws IOException {
        System.out.println("UDP - Sending to " + address);
        DatagramPacket packet = new DatagramPacket(data, data.length, address);

        synchronized (mSocketLock) {
            mSocket.send(packet);
        }
    }

    @Override
    public void run() {
        while (mRunning) {
            try {
                mSocket.receive(mReceivePacket);
            } catch (SocketException e) {
                System.out.println("UDP - Socket closed");
                return;
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            byte[] buf = mReceivePacket.getData();
            if (buf[0] == 'P') {
                System.out.println("UDP - Ping received from " + mReceivePacket.getSocketAddress());
                // we were pinged, respond
                buf[0] = 'R';

                try {
                    sendUdp(buf, mReceivePacket.getSocketAddress());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
