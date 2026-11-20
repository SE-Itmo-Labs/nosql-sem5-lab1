#!/usr/bin/env bash
set -euo pipefail
cd -- "$(dirname -- "$0")/.."

usage='mongo_explain_comparsion.sh [--demo | USER RULE_ID FROM TO]'
if [[ "${1:-}" == --help || "${1:-}" == -h ]]; then
  echo "$usage"
  exit 0
fi

collection=notifications
if [[ "$#" == 1 && "$1" == --demo ]]; then
  collection=notifications_explain_demo
  set -- user 7 2026-11-01T00:00:00Z 2026-11-08T00:00:00Z
elif [[ "$#" != 0 && "$#" != 4 ]]; then
  echo "$usage" >&2
  exit 2
fi

uri="${MONGO_EXPLAIN_URI:-mongodb://127.0.0.1:27017/somedb_mongo?directConnection=true&serverSelectionTimeoutMS=5000}"
docker compose --env-file "${MONGO_EXPLAIN_ENV_FILE:-credentials.env}" exec -T \
  -e "COLLECTION=$collection" -e "LOGIN=${1-user}" -e "RULE_ID=${2-}" -e "FROM=${3-}" -e "TO=${4-}" \
  mongodb mongosh --quiet --norc "$uri" --file /dev/stdin <<'MONGO'
const { COLLECTION, LOGIN, RULE_ID, FROM, TO } = process.env;
const collection = db.getCollection(COLLECTION);
const indexName = "notifications_user_rule_updated_id";
if (!collection.getIndexes().some(index => index.name === indexName)) {
  throw new Error("В " + COLLECTION + " нет индекса " + indexName
    + ". Собери свежий JAR через ./gradlew bootJar, затем пересобери контейнер spring-app.");
}
let processingRuleId, from, to;
if (!RULE_ID && !FROM && !TO) {
  const [sample] = collection.find(
    { userId: LOGIN, processingRuleId: { $type: "number" }, updatedAt: { $type: "date" } },
    { processingRuleId: 1, updatedAt: 1 }
  ).limit(1).maxTimeMS(5000).toArray();
  if (!sample) throw new Error("В notifications нет уведомлений с правилом обработки для " + LOGIN + ".");
  processingRuleId = sample.processingRuleId;
  from = new Date(sample.updatedAt);
  from.setUTCHours(0, 0, 0, 0);
  to = new Date(from.getTime() + 86400000);
} else {
  from = new Date(FROM);
  to = new Date(TO);
  if (!LOGIN || !/^[1-9][0-9]*$/.test(RULE_ID) || BigInt(RULE_ID) > 9223372036854775807n || !(from < to)) {
    throw new Error("Укажи логин, ID правила и даты в порядке FROM TO.");
  }
  processingRuleId = NumberLong(RULE_ID);
}

const filter = { userId: LOGIN, processingRuleId, updatedAt: { $gte: from, $lt: to } };
const explain = hint => collection.find(filter, { actionLog: 0 })
  .sort({ updatedAt: -1, _id: -1 }).limit(20).hint(hint).maxTimeMS(5000)
  .explain("executionStats").executionStats;

const indexed = explain(indexName);
const scanned = explain({ $natural: 1 });
const row = (label, left, right) => print(label.padEnd(24) + String(left).padStart(12) + String(right).padStart(12));

row("Метрика", "Без индекса", "С индексом");
row("Документов просмотрено", scanned.totalDocsExamined, indexed.totalDocsExamined);
row("Документов найдено", scanned.nReturned, indexed.nReturned);
row("Время, мс", scanned.executionTimeMillis, indexed.executionTimeMillis);
MONGO
