package com.wejuai.message.repository;

import com.wejuai.entity.mysql.ChatUserRecord;
import com.wejuai.entity.mysql.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author ZM.Wang
 */
public interface ChatUserRecordRepository extends JpaRepository<ChatUserRecord, String> {

    ChatUserRecord findBySenderAndRecipientAndDelFalse(User sender, User recipient);

}
