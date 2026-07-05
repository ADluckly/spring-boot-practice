package com.example.spring_boot_demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.spring_boot_demo.model.UploadTask;
import com.example.spring_boot_demo.model.UploadTaskStatus;

public interface UploadTaskRepository extends JpaRepository<UploadTask, Long> {

    List<UploadTask> findTop100ByStatusOrderByCreatedAtAsc(UploadTaskStatus status);
}
