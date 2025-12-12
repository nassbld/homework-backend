package com.homework.backend.controllers;

import com.homework.backend.dto.ConversationSnippetDto;
import com.homework.backend.models.ChatMessage;
import com.homework.backend.models.Conversation;
import com.homework.backend.models.User;
import com.homework.backend.services.ConversationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/conversations")
public class ConversationController {

    private final ConversationService conversationService;

    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @GetMapping
    public ResponseEntity<List<ConversationSnippetDto>> getMyConversations(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(conversationService.findConversationsForUser(currentUser.getId()));
    }

    @GetMapping("/{conversationId}/messages")
    public ResponseEntity<List<ChatMessage>> getMessageHistory(
            @PathVariable Long conversationId,
            @AuthenticationPrincipal User currentUser) {

        List<ChatMessage> messages = conversationService.findMessagesForConversation(conversationId, currentUser.getId());

        return ResponseEntity.ok(messages);
    }

    @PostMapping("/start")
    public ResponseEntity<Map<String, Long>> startOrGetConversation(
            @RequestBody Map<String, Long> payload,
            @AuthenticationPrincipal User currentUser) {

        Long otherUserId = payload.get("otherUserId");
        if (otherUserId == null) {
            return ResponseEntity.badRequest().build();
        }

        Conversation conversation = conversationService.startOrGetConversation(currentUser.getId(), otherUserId);

        Map<String, Long> response = new HashMap<>();
        response.put("conversationId", conversation.getId());

        return ResponseEntity.ok(response);
    }

}
