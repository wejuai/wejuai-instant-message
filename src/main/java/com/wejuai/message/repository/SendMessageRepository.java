package com.wejuai.message.repository;

import com.wejuai.entity.mongo.SendMessage;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author ZM.Wang
 */
public interface SendMessageRepository extends MongoRepository<SendMessage, String> {
}
