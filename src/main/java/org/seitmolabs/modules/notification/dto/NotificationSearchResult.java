package org.seitmolabs.modules.notification.dto;

public record NotificationSearchResult(NotificationSearchPage page, CacheStatus cacheStatus) {
    public enum CacheStatus { HIT, MISS, BYPASS }
}
