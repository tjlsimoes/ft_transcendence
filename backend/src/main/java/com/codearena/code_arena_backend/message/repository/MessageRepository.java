package com.codearena.code_arena_backend.message.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.codearena.code_arena_backend.message.entity.Message;

public interface MessageRepository extends JpaRepository<Message, Long> {
    
    @Query("SELECT m FROM Message m WHERE " + 
            "(m.senderId = :user1Id AND m.recipientId = :user2Id) OR " +
            "(m.senderId = :user2Id AND m.recipientId = :user1Id) " +
            "ORDER BY m.createdAt DESC")
    Page<Message> findBySenderAndRecipient(Long user1Id, Long user2Id, Pageable pageable);
}
