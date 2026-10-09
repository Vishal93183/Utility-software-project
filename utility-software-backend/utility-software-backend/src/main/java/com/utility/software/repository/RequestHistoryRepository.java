package com.utility.software.repository;

import com.utility.software.entity.RequestHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RequestHistoryRepository extends JpaRepository<RequestHistory, Long> {
    List<RequestHistory> findTop10ByOrderByCreatedAtDesc();
}
