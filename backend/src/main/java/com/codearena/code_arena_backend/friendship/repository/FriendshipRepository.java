package com.codearena.code_arena_backend.friendship.repository;

import com.codearena.code_arena_backend.friendship.entity.Friendship;
import com.codearena.code_arena_backend.friendship.entity.FriendshipId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, FriendshipId> {

    List<Friendship> findByUserIdAndStatus(Long userId, String status);

    boolean existsByUserIdAndFriendId(Long userId, Long friendId);

    void deleteByUserIdAndFriendId(Long userId, Long friendId);
}
