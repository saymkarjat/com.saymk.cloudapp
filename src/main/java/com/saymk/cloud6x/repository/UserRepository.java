package com.saymk.cloud6x.repository;

import com.saymk.cloud6x.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    @Modifying
    @Query("UPDATE User u SET u.storageSize = u.storageSize + :size WHERE u.id = :userId AND u.storageSize + :size <= :maxCapacity")
    int increaseStorageSize(@Param("userId") Long userId, @Param("size") Long size, @Param("maxCapacity") Long maxCapacity);
}
