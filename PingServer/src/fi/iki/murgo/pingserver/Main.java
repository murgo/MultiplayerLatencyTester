package fi.iki.murgo.pingserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {

    public static void main(String[] args) {
	    UdpPinger udpPinger = new UdpPinger();
        udpPinger.open();

        TcpPinger tcpPinger = new TcpPinger();
        tcpPinger.open();

        System.out.println("Type 'quit' to exit: ");
        InputStreamReader s = new InputStreamReader(System.in);
        BufferedReader in = new BufferedReader(s);

        String line = "";
        while (!(line.toLowerCase().equals("quit"))) {
            try {
                line = in.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Thread.yield();
        }

        udpPinger.close();
        tcpPinger.close();
    }
}
