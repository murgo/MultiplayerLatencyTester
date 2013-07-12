package fi.iki.murgo.pingserver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class TcpPinger implements Runnable {

    private static final int PORT = 56667;
    private DataInputStream mInput;
    private DataOutputStream mOutput;
    private boolean mRunning;
    private ServerSocket mSocket;

    public void open() {
        mRunning = true;
        new Thread(this).start();
    }

    public void close() {
        mRunning = false;

        try {
            mSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            mSocket = new ServerSocket(PORT);
            mSocket.setReceiveBufferSize(9);
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (mRunning) {
            Socket socket;
            try {
                socket = mSocket.accept();
                System.out.println("TCP - Opening socket to " + socket.getRemoteSocketAddress());
                socket.setKeepAlive(false);
                socket.setReceiveBufferSize(9);
                socket.setSendBufferSize(9);
                socket.setTcpNoDelay(true);
            } catch (SocketException e) {
                System.out.println("TCP - Listening socket closed");
                return;
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            new Thread(handle(socket)).start();
        }
    }

    public Runnable handle(final Socket socket) {
        return new Runnable() {
            @Override
            public void run() {
                try {
                    mInput = new DataInputStream(socket.getInputStream());
                    mOutput = new DataOutputStream(socket.getOutputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                    try {
                        socket.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    return;
                }

                while (mRunning) {
                    Byte b;
                    long nanoTime;
                    try {
                        b = mInput.readByte();
                        nanoTime = mInput.readLong();
                    } catch (SocketException e) {
                        System.out.println("TCP - Client socket closed");
                        break;
                    } catch (EOFException e) {
                        System.out.println("TCP - Client socket closed (EOF)");
                        break;
                    } catch (IOException e) {
                        e.printStackTrace();
                        break;
                    }

                    System.out.println("TCP - Data received from " + socket.getRemoteSocketAddress() + ": " + b);

                    if (b == 'P') {
                        // we were pinged, respond
                        try {
                            mOutput.writeByte((byte)'R');
                            mOutput.writeLong(nanoTime);
                            System.out.println("TCP - Data sent to " + socket.getRemoteSocketAddress());
                        } catch (IOException e) {
                            e.printStackTrace();
                            break;
                        }
                    } else {
                        // invalid packet
                        System.out.println("TCP - Invalid packet: " + b);
                        break;
                    }
                }

                try {
                    socket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        };
    }
}
