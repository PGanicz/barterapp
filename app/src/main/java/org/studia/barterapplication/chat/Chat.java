package org.studia.barterapplication.chat;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

public class Chat {
    private String sender;
    private String receiver;
    private String message;
    private boolean isseen;

    @ServerTimestamp
    private Timestamp timestamp;

    public Chat(String sender, String receiver, String message, boolean isseen) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.isseen = isseen;
    }

    public Chat() {
    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getMessage() {
        return message;
    }

    public boolean isIsseen() {
        return isseen;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
