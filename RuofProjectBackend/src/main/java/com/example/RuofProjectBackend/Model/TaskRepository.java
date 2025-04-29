package com.example.RuofProjectBackend.Model;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
   // Finds tasks by userId and category
List<Task> findByUserIdAndCategory(Long userId, String category);

// Finds tasks by userId only (for the "All" category filter)
List<Task> findByUserId(Long userId);

List<Task> findByUserIdAndCategoryIn(Long userId, List<String> categories);




}
