package org.seitmolabs.modules.notification.dto;

import java.util.List;

public record NotificationReadAnalytics(Summary summary, List<Day> byDay) {
    public record Summary(long totalCount, long readCount, Double averageMs) {
    }

    public record Day(String day, long totalCount, long readCount, Double averageMs) {
    }
}
