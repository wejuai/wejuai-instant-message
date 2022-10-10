package com.wejuai.message.repository;

import com.wejuai.entity.mysql.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author ZM.Wang
 */
public interface UserRepository extends JpaRepository<User, String> {

    User getUserByAccounts_Id(String accountsId);
}
