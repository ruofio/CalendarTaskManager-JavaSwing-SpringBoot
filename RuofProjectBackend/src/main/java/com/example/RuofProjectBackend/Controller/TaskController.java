package com.example.RuofProjectBackend.Controller;

import com.example.RuofProjectBackend.Model.Task;
import com.example.RuofProjectBackend.Model.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskRepository taskRepository;

    @PostMapping
    public Task createTask(@RequestBody Task task) {
        return taskRepository.save(task);
    }

    @GetMapping("/{userId}")
    public List<Task> getUserTasks(@PathVariable Long userId, @RequestParam(required = false) List<String> categories) {
        if (categories == null || categories.isEmpty() || categories.contains("All")) {
            return taskRepository.findByUserId(userId);
        } else {
            return taskRepository.findByUserIdAndCategoryIn(userId, categories);
        }
    }
    

      // New PUT method to update an existing task
      @PutMapping("/{taskId}")
      public ResponseEntity<Task> updateTask(@PathVariable Long taskId, @RequestBody Task updatedTask) {
          Optional<Task> existingTaskOptional = taskRepository.findById(taskId);
          
          if (!existingTaskOptional.isPresent()) {
              return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // Task not found
          }
  
          Task existingTask = existingTaskOptional.get();
  
          // Update the existing task with the values from the request body
          existingTask.setName(updatedTask.getName());
          existingTask.setCategory(updatedTask.getCategory());
          existingTask.setDescription(updatedTask.getDescription());
          existingTask.setDate(updatedTask.getDate());
          existingTask.setUserId(updatedTask.getUserId());
          existingTask.setCompleted(updatedTask.isCompleted());
          existingTask.setNotes(updatedTask.getNotes());
          // Save the updated task
          Task savedTask = taskRepository.save(existingTask);
          
          return ResponseEntity.ok(savedTask); // Return the updated task as response
      }
  
    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long taskId) {
        if (taskId == null) {
            return ResponseEntity.badRequest().build();  // Handle invalid ID or null case
        }
    
        Optional<Task> task = taskRepository.findById(taskId);
        if (task.isPresent()) {
            taskRepository.deleteById(taskId);
            return ResponseEntity.noContent().build();  // Success response
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();  // Task not found
    }

    @GetMapping("/count/{userId}")
public ResponseEntity<Map<String, Integer>> getTaskStatusCount(@PathVariable Long userId) {
    List<Task> tasks = taskRepository.findByUserId(userId);

    int completed = 0;
    int inProgress = 0;

    for (Task task : tasks) {
        if (task.isCompleted()) {
            completed++;
        } else {
            inProgress++;
        }
    }

    Map<String, Integer> response = new HashMap<>();
    response.put("completed", completed);
    response.put("inProgress", inProgress);

    return ResponseEntity.ok(response);
}

@RestController
@RequestMapping("/api/tasks/stats")
public class TaskStatsController {

    @Autowired
    private TaskRepository taskRepository;

    @GetMapping("/{userId}")
    public ResponseEntity<Map<String, Map<String, Integer>>> getStats(@PathVariable Long userId) {
        List<Task> tasks = taskRepository.findByUserId(userId);

        Map<String, Integer> completionStats = new HashMap<>();
        Map<String, Integer> categoryStats = new HashMap<>();
        Map<String, Integer> dayStats = new HashMap<>();

        for (Task task : tasks) {
            // Completion
            String status = task.isCompleted() ? "Completed" : "In Progress";
            completionStats.put(status, completionStats.getOrDefault(status, 0) + 1);

            // Category
            String category = task.getCategory();
            categoryStats.put(category, categoryStats.getOrDefault(category, 0) + 1);

            // Day of Week
            String dayOfWeek = task.getDate().getDayOfWeek().toString(); // e.g. MONDAY
            dayStats.put(dayOfWeek, dayStats.getOrDefault(dayOfWeek, 0) + 1);
        }

        Map<String, Map<String, Integer>> response = new HashMap<>();
        response.put("completionStats", completionStats);
        response.put("categoryStats", categoryStats);
        response.put("dayStats", dayStats);

        return ResponseEntity.ok(response);
    }
}

}


    

