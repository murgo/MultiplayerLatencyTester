package fi.iki.murgo.ping;

import com.google.android.gms.games.multiplayer.Participant;

public class ParticipantInfo {
    public Participant participant;

    public long reliableRtt;
    public long unreliableRtt;

    public int reliablePacketsSent;
    public int reliableRepliesReceived;

    public int unreliablePacketsSent;
    public int unreliableRepliesReceived;

    public ParticipantInfo(Participant p) {
        participant = p;
    }

    public String getReliableInfo() {
        return "REL: " + String.format("%1$,.2f", reliableRtt / 1e6) + " ms (" + reliableRepliesReceived + "/" + reliablePacketsSent + ")";
    }

    public String getUnreliableInfo() {
        return "UNREL: " + String.format("%1$,.2f", unreliableRtt / 1e6) + " ms (" + unreliableRepliesReceived + "/" + unreliablePacketsSent + ")";
    }
}
