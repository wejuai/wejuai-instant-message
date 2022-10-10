package com.wejuai.message.repository;

import com.wejuai.entity.mysql.Accounts;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountsRepository extends JpaRepository<Accounts, String> {
}
