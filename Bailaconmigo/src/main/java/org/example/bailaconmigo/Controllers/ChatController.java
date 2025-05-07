package org.example.bailaconmigo.Controllers;

import org.example.bailaconmigo.DTOs.ChatMessageDto;
import org.example.bailaconmigo.DTOs.MessageDto;
import org.example.bailaconmigo.Entities.Message;
import org.example.bailaconmigo.Services.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @PostMapping("/send")
    public Message sendMessage(@RequestBody MessageDto dto) {
        return chatService.sendMessage(dto);
    }

    @GetMapping("/history")
    public List<ChatMessageDto> getChatHistory(@RequestParam Long user1Id, @RequestParam Long user2Id) {
        return chatService.getChatHistory(user1Id, user2Id);
    }
}
