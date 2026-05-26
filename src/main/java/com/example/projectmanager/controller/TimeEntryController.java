package com.example.projectmanager.controller;

import com.example.projectmanager.model.TimeEntry;
import com.example.projectmanager.service.TimeEntryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/time")
public class TimeEntryController {
    private final TimeEntryService timeEntryService;

    public TimeEntryController(TimeEntryService timeEntryService) {
        this.timeEntryService = timeEntryService;
    }

    @GetMapping("/task/{taskId}")
    public String list(@PathVariable Long taskId, Model model) {
        model.addAttribute("entries", timeEntryService.getByTask(taskId));
        model.addAttribute("totalMinutes", timeEntryService.getTotalMinutes(taskId));
        model.addAttribute("taskId", taskId);
        return "time-entries";
    }

    @PostMapping("/start")
    public String start(@RequestParam Long taskId) {
        TimeEntry entry = new TimeEntry();
        entry.setTaskId(taskId);
        entry.setUserId(1L);
        entry.setStartTime(LocalDateTime.now());
        timeEntryService.startTimer(entry);
        return "redirect:/time/task/" + taskId;
    }

    @GetMapping("/stop/{id}/{taskId}")
    public String stop(@PathVariable Long id, @PathVariable Long taskId) {
        timeEntryService.stopTimer(id);
        return "redirect:/time/task/" + taskId;
    }
}