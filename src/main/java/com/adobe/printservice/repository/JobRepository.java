package com.adobe.printservice.repository;

import com.adobe.printservice.model.Job;
import com.adobe.printservice.model.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.List;

public interface JobRepository extends JpaRepository<Job, String> {

    @Query(value = """
            SELECT *
            FROM job
            WHERE status = 'QUEUED'
            ORDER BY created_at
            LIMIT 1
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    Optional<Job> findNextQueuedForUpdate();

    List<Job> findByStatus(JobStatus status);

    long countByStatus(JobStatus status);
}
