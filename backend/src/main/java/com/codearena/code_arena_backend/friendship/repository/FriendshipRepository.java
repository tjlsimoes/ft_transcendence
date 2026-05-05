package com.codearena.code_arena_backend.friendship.repository;

import com.codearena.code_arena_backend.friendship.entity.Friendship;
import com.codearena.code_arena_backend.friendship.entity.FriendshipId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, FriendshipId> {

    List<Friendship> findByUserIdAndStatus(Long userId, String status);

    boolean existsByUserIdAndFriendId(Long userId, Long friendId);

    void deleteByUserIdAndFriendId(Long userId, Long friendId);

    /**
     * Inserts a friendship row, silently ignoring duplicates.
     * Uses the PK constraint (user_id, friend_id) as the conflict target,
     * eliminating the check-then-insert race condition.
     */
    @Modifying
    @Query(value = "INSERT INTO friendships (user_id, friend_id, status) "
            + "VALUES (:userId, :friendId, :status) "
            + "ON CONFLICT (user_id, friend_id) DO NOTHING",
            nativeQuery = true)
    void insertIgnoreConflict(@Param("userId") Long userId,
                              @Param("friendId") Long friendId,
                              @Param("status") String status);

    @Query("SELECT f FROM Friendship f WHERE (f.userId = :userId OR f.friendId = :userId) AND f.status = 'ACCEPTED'")
    List<Friendship> findAcceptedByUserId(Long userId);

    @Query("SELECT f FROM Friendship f WHERE f.friendId = :userId AND f.status = 'PENDING'")
    List<Friendship> findPendingForUserId(Long userId);
}
