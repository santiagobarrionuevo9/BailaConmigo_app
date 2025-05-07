package org.example.bailaconmigo.DTOs;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatMessageDto {
    private Long senderId;
    private String senderName;
    private Long recipientId;
    private String recipientName;
    private String content;
    private LocalDateTime timestamp;


}
