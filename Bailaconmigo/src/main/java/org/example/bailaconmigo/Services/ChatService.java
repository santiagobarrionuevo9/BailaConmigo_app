package org.example.bailaconmigo.Services;

import org.example.bailaconmigo.DTOs.ChatMessageDto;
import org.example.bailaconmigo.DTOs.MessageDto;
import org.example.bailaconmigo.Entities.Match;
import org.example.bailaconmigo.Entities.Message;
import org.example.bailaconmigo.Entities.User;
import org.example.bailaconmigo.Repositories.MatchRepository;
import org.example.bailaconmigo.Repositories.MessageRepository;
import org.example.bailaconmigo.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private UserRepository userRepository;

    public Message sendMessage(MessageDto dto) {
        User sender = userRepository.findById(dto.getSenderId())
                .orElseThrow(() -> new RuntimeException("Remitente no encontrado"));
        User recipient = userRepository.findById(dto.getRecipientId())
                .orElseThrow(() -> new RuntimeException("Destinatario no encontrado"));

        // Verificar que hay un match mutuo
        boolean isMatched = matchRepository.findByLikerIdAndLikedUserId(sender.getId(), recipient.getId())
                .map(Match::isMatched).orElse(false)
                || matchRepository.findByLikerIdAndLikedUserId(recipient.getId(), sender.getId())
                .map(Match::isMatched).orElse(false);

        if (!isMatched) {
            throw new RuntimeException("No se puede enviar mensaje sin un match mutuo");
        }

        Message message = new Message();
        message.setSender(sender);
        message.setRecipient(recipient);
        message.setContent(dto.getContent());
        message.setTimestamp(LocalDateTime.now());

        return messageRepository.save(message);
    }

    public List<ChatMessageDto> getChatHistory(Long user1Id, Long user2Id) {
        List<Message> messages = messageRepository.findChatHistory(user1Id, user2Id);
        return messages.stream().map(msg -> {
            ChatMessageDto dto = new ChatMessageDto();
            dto.setSenderId(msg.getSender().getId());
            dto.setSenderName(msg.getSender().getFullName());
            dto.setRecipientId(msg.getRecipient().getId());
            dto.setRecipientName(msg.getRecipient().getFullName());
            dto.setContent(msg.getContent());
            dto.setTimestamp(msg.getTimestamp());
            return dto;
        }).toList();
    }

}
