package gtp.projecttracker.event;

import gtp.projecttracker.model.jpa.Task;

public record TaskOverdueEvent(Task task, int daysOverdue) {
}