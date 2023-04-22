package com.kreuterkeule.MeatMessenger.repositories;

import com.kreuterkeule.MeatMessenger.models.MessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<MessageEntity, Long> {

    @Query(value = "SELECT * FROM messages m WHERE m.value_from = :token OR m.value_to = :token ORDER BY m.create_date ASC", nativeQuery = true)
    List<MessageEntity> getAllMessagesByUserToken(@Param("token") String token);

    @Query(value = "SELECT * FROM messages m WHERE m.value_from = :token OR m.value_to = :token ORDER BY m.create_date ASC LIMIT 100", nativeQuery = true)
    List<MessageEntity> getRecentMessagesByUserToken(@Param("token") String token);

}
