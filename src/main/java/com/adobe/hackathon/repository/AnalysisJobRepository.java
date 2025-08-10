package com.adobe.hackathon.repository;

import com.adobe.hackathon.model.entity.AnalysisJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnalysisJobRepository extends JpaRepository<AnalysisJob, Long> {

    Optional<AnalysisJob> findByJobId(String jobId);

    List<AnalysisJob> findByStatus(String status);

    @Query("SELECT COUNT(aj) FROM AnalysisJob aj WHERE aj.status = :status")
    long countByStatus(@Param("status") String status);

    @Query("SELECT aj FROM AnalysisJob aj WHERE aj.status IN :statuses ORDER BY aj.createdAt DESC")
    List<AnalysisJob> findByStatusIn(@Param("statuses") List<String> statuses);

    @Query("SELECT aj FROM AnalysisJob aj ORDER BY aj.createdAt DESC")
    List<AnalysisJob> findAllOrderByCreatedAtDesc();
}