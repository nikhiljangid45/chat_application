package com.example.chatapplication.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

public class ChatMessageModel {
    String message;
    String senderId;
    Timestamp timestamp;

    public ChatMessageModel(String message, String senderId, Timestamp timestamp) {
        this.message = message;
        this.senderId = senderId;
        this.timestamp = timestamp;
    }

    public ChatMessageModel() {
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }




    // Other existing fields...

    // Add a field to store the document ID
    private String documentId;

    // Constructor
    public ChatMessageModel(String documentId) {
        // Other assignments...
        this.documentId = documentId;
    }

    // Getter for the document ID
    public String getDocumentId() {
        return documentId;
    }

    // Static method to create ChatMessageModel from DocumentSnapshot
    public static ChatMessageModel fromDocumentSnapshot(DocumentSnapshot documentSnapshot) {
        // Parse fields from the DocumentSnapshot and create ChatMessageModel instance
        String message = documentSnapshot.getString("message");
        String senderId = documentSnapshot.getString("senderId");
        // Other fields...

        String documentId = documentSnapshot.getId(); // Get the document ID

        return new ChatMessageModel(documentId);
    }
}
