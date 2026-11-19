// Work in a new database on each run so the experiment leaves application data alone.
const demo = db.getSiblingDB("nosql_lab2_demo_" + new ObjectId().toHexString());
print("Демо-база: " + demo.getName());

const notifications = demo.notifications;
const rules = demo.processing_rules;
const from = ISODate("2026-01-01T00:00:00Z");
const to = ISODate("2026-02-01T00:00:00Z");
const created = { type: "CREATED", actorId: "system", at: from };
const applied = { type: "RULE_APPLIED", actorId: "admin", at: from };
const read = { type: "READ", actorId: "user", at: from };

rules.insertOne({ _id: 7, appliedCount: 0 });
notifications.insertMany([
    { _id: 1, userId: "user", processingRuleId: 7, updatedAt: from, tags: ["booking", "confirmed"], actionLog: [created, applied] },
    { _id: 2, userId: "user", processingRuleId: 7, updatedAt: from, tags: ["booking", "confirmed"], actionLog: [created, applied, read] },
    { _id: 3, userId: "user", processingRuleId: 8, updatedAt: from, tags: ["booking", "cancelled"], actionLog: [created, read] },
    { _id: 4, userId: "guest", processingRuleId: 7, updatedAt: from, tags: ["booking", "confirmed"], actionLog: [created, applied] },
    { _id: 5, userId: "user", updatedAt: from, tags: [], actionLog: [created] },
    { _id: 6, userId: "user", updatedAt: from, tags: [], actionLog: [created] }
]);

const filter = {
    userId: "user",
    processingRuleId: 7,
    updatedAt: { $gte: from, $lt: to },
    actionLog: { $elemMatch: { type: "RULE_APPLIED", actorId: "admin" } }
};
const sort = { updatedAt: -1, _id: -1 };
const page = offset => notifications.find(filter, { _id: 1 }).sort(sort).skip(offset).limit(1).toArray();
assert.eq(2, page(0)[0]._id);
assert.eq(1, page(1)[0]._id);
assert.eq(0, notifications.countDocuments({
    userId: "user", actionLog: { $elemMatch: { type: "READ", actorId: "admin" } }
}));
print("Поиск: страницы [2], [1]; READ от admin не найден.");

const analytics = notifications.aggregate([
    { $match: { userId: "user", updatedAt: { $gte: from, $lt: to } } },
    { $project: { tags: { $setUnion: [{ $ifNull: ["$tags", []] }, []] } } },
    { $unwind: "$tags" },
    { $group: { _id: "$tags", count: { $sum: 1 } } },
    { $sort: { count: -1, _id: 1 } },
    { $limit: 3 }
]).toArray();
assert.eq([{ _id: "booking", count: 3 }, { _id: "confirmed", count: 2 }, { _id: "cancelled", count: 1 }], analytics);
printjson(analytics);

const indexFilter = { userId: "user", processingRuleId: 7, updatedAt: { $gte: from, $lt: to } };
const before = notifications.find(indexFilter).sort(sort).explain("executionStats").executionStats;
const indexName = notifications.createIndex({ userId: 1, processingRuleId: 1, updatedAt: -1, _id: -1 });
const after = notifications.find(indexFilter).sort(sort).hint(indexName).explain("executionStats").executionStats;
assert.eq(before.nReturned, after.nReturned);
assert(before.totalDocsExamined > after.totalDocsExamined);
printjson({
    scannedBefore: before.totalDocsExamined,
    scannedAfter: after.totalDocsExamined,
    indexKeysAfter: after.totalKeysExamined
});

function applyRule(id, commit) {
    const session = demo.getMongo().startSession();
    try {
        const sessionDb = session.getDatabase(demo.getName());
        session.startTransaction();
        sessionDb.processing_rules.updateOne({ _id: 7 }, { $inc: { appliedCount: 1 } });
        if (!commit) {
            // Simulate a failure after updating the rule and before appending the journal entry.
            session.abortTransaction();
            return;
        }
        sessionDb.notifications.updateOne({ _id: id }, {
            $set: { processingRuleId: 7 },
            $addToSet: { tags: { $each: ["booking", "confirmed"] } },
            $push: { actionLog: applied }
        });
        session.commitTransaction();
    } finally {
        session.endSession();
    }
}
applyRule(5, true);
applyRule(6, false);
assert.eq(1, rules.findOne({ _id: 7 }).appliedCount);
assert.eq(7, notifications.findOne({ _id: 5 }).processingRuleId);
assert.eq("RULE_APPLIED", notifications.findOne({ _id: 5 }).actionLog[1].type);
assert.eq(1, notifications.findOne({ _id: 6 }).actionLog.length);
assert.eq(0, notifications.countDocuments({ _id: 6, processingRuleId: { $exists: true } }));
print("Транзакция: ID 5 применён, ID 6 откатан, счётчик правила равен 1.");

const legacy = demo.legacy_notifications;
legacy.insertMany([
    { _id: 1, tag: "booking" },
    { _id: 2, tag: "booking", tags: ["booking", "confirmed"] },
    { _id: 3, tags: [], schemaVersion: 2 }
]);
function migrateTags() {
    return legacy.updateMany({ tag: { $type: "string" } }, [
        { $set: { tags: { $setUnion: [{ $ifNull: ["$tags", []] }, ["$tag"]] }, schemaVersion: 2 } },
        { $unset: "tag" }
    ]).modifiedCount;
}
assert.eq(2, migrateTags());
assert.eq(0, migrateTags());
assert.eq(["booking", "confirmed"], legacy.findOne({ _id: 2 }).tags.sort());
assert.commandWorked(demo.runCommand({
    collMod: legacy.getName(),
    validator: {
        $jsonSchema: {
            bsonType: "object",
            required: ["tags", "schemaVersion"],
            properties: {
                tags: { bsonType: "array", items: { bsonType: "string" } },
                schemaVersion: { enum: [2] }
            }
        }
    }
}));
let validationErrorCode;
try {
    legacy.insertOne({ _id: 4, tags: "booking", schemaVersion: 2 });
} catch (error) {
    validationErrorCode = error.code;
}
assert.eq(121, validationErrorCode);
print("Миграция: изменено 2, повторно 0; JSON Schema отклонила строку вместо массива.");
