package com.nihit.craft_connect.repository;

import com.nihit.craft_connect.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    @Query("SELECT c FROM Conversation c WHERE " +
            "(c.userOne.id = :userA AND c.userTwo.id = :userB) OR " +
            "(c.userOne.id = :userB AND c.userTwo.id = :userA)")
    Optional<Conversation> findBetweenUsers(@Param("userA") Long userA, @Param("userB") Long userB);

    @Query("SELECT c FROM Conversation c WHERE c.userOne.id = :userId OR c.userTwo.id = :userId " +
            "ORDER BY c.lastMessageAt DESC")
    List<Conversation> findAllForUser(@Param("userId") Long userId);
}
