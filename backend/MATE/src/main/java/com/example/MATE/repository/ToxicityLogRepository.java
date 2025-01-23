package com.example.MATE.repository;

import com.example.MATE.model.ToxicityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ToxicityLogRepository extends JpaRepository<ToxicityLog, Integer> {
    boolean existsBySpeechLog_LogId(Integer logId);

}
