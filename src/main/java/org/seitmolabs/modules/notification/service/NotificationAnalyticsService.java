package org.seitmolabs.modules.notification.service;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.seitmolabs.modules.notification.domain.Notification;
import org.seitmolabs.modules.notification.dto.NotificationReadAnalytics;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationAnalyticsService {
    private final MongoTemplate mongoTemplate;

    public NotificationReadAnalytics readTimes(int days) {
        LocalDate end = LocalDate.now(ZoneOffset.UTC).plusDays(1);
        var period = Criteria.where("createdAt")
                .gte(Date.from(end.minusDays(days).atStartOfDay().toInstant(ZoneOffset.UTC)))
                .lt(Date.from(end.atStartOfDay().toInstant(ZoneOffset.UTC)));

        // Extract only valid READ timestamps before finding the first read in each document.
        var firstRead = Document.parse("""
                { "$set": { "firstReadAt": { "$min": { "$map": {
                  "input": { "$filter": {
                    "input": { "$ifNull": ["$actionLog", []] },
                    "as": "action",
                    "cond": { "$and": [
                      { "$eq": ["$$action.type", "READ"] },
                      { "$eq": [{ "$type": "$$action.at" }, "date"] },
                      { "$gte": ["$$action.at", "$createdAt"] }
                    ] }
                  } },
                  "as": "read", "in": "$$read.at"
                } } } } }
                """);
        var bucketDay = Document.parse("""
                { "$dateToString": { "format": "%Y-%m-%d", "date": "$createdAt", "timezone": "UTC" } }
                """);
        var aggregation = Aggregation.newAggregation(
                Aggregation.match(period),
                Aggregation.stage(firstRead),
                Aggregation.stage(Document.parse("""
                        { "$project": { "createdAt": 1,
                          "elapsedMs": { "$subtract": ["$firstReadAt", "$createdAt"] } } }
                        """)),
                Aggregation.stage(new Document("$facet", new Document("summary", List.of(
                        countReads(null)))
                        .append("byDay", List.of(
                                countReads(bucketDay),
                                new Document("$sort", new Document("_id", -1)),
                                Document.parse("""
                                        { "$project": { "_id": 0, "day": "$_id", "totalCount": 1,
                                          "readCount": 1, "averageMs": 1 } }
                                        """)))))
        );
        var result = mongoTemplate.aggregate(aggregation, Notification.COLLECTION_NAME, Facets.class)
                .getUniqueMappedResult();
        if (result == null || result.summary().isEmpty()) {
            return new NotificationReadAnalytics(new NotificationReadAnalytics.Summary(0, 0, null), List.of());
        }
        return new NotificationReadAnalytics(result.summary().getFirst(), result.byDay());
    }

    private Document countReads(Document groupKey) {
        return new Document("$group", new Document("_id", groupKey)
                .append("totalCount", new Document("$sum", 1))
                .append("readCount", Document.parse("""
                        { "$sum": { "$cond": [{ "$ne": ["$elapsedMs", null] }, 1, 0] } }
                        """))
                .append("averageMs", new Document("$avg", "$elapsedMs")));
    }

    private record Facets(List<NotificationReadAnalytics.Summary> summary, List<NotificationReadAnalytics.Day> byDay) {
    }
}
