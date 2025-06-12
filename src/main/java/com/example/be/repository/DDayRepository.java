package com.example.be.repository;

import com.example.be.domain.DDay;
import com.example.be.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface DDayRepository extends JpaRepository<DDay, Long> {

    List<DDay> findDDayByUser(User user);
}
