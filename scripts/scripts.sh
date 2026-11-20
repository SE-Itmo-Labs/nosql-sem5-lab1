docker compose --env-file credentials.env exec mongodb mongosh somedb_mongo

db.notifications.getIndexes().find(i =>
  i.name === "notifications_user_rule_updated_id"
)



var sample = db.notifications.findOne({
  userId: "user",
  processingRuleId: { $type: "number" },
  updatedAt: { $type: "date" }
})

var from = new Date(sample.updatedAt)
from.setUTCHours(0, 0, 0, 0)
var to = new Date(from.getTime() + 86400000)

var filter = {
  userId: "user",
  processingRuleId: sample.processingRuleId,
  updatedAt: { $gte: from, $lt: to }
}

printjson(filter)






var query = () => db.notifications.find(filter, { actionLog: 0 })
  .sort({ updatedAt: -1, _id: -1 })

var scan = query().hint({ $natural: 1 }).explain("executionStats")
var index = query().hint("notifications_user_rule_updated_id").explain("executionStats")

for (var [mode, plan] of [["Без индекса", scan], ["С индексом", index]]) {
  var s = plan.executionStats
  printjson({
    mode,
    найдено: s.nReturned,
    просмотреноДокументов: s.totalDocsExamined,
    просмотреноКлючей: s.totalKeysExamined,
    времяМс: s.executionTimeMillis
  })
}



db.notification_search_versions.findOne({ _id: "user" })

db.notifications.find(
  { userId: "user", status: "SENT" },
  { _id: 1, title: 1, status: 1, updatedAt: 1 }
).sort({ updatedAt: -1, _id: -1 }).limit(1)


curl -si -H 'X-Username: user' -H 'X-Password: 12345' \
  'http://localhost:16767/api/v1/notifications/search?status=SENT&size=1'


docker compose --env-file credentials.env exec redis-db \
  redis-cli --scan --pattern 'notification:search:v1:*'

docker compose --env-file credentials.env exec redis-db \
  redis-cli TTL 'ПОДСТАВЬ_КЛЮЧ'



curl -si -X POST -H 'X-Username: user' -H 'X-Password: 12345' \
  'http://localhost:16767/api/v1/notifications/ID/read'


db.notification_search_versions.findOne({ _id: "user" })