#!/usr/bin/env bash
set -euo pipefail
cd -- "$(dirname -- "$0")/.."

usage='mongo_explain_comparsion.sh [--demo | USER CATEGORY_ID FROM TO]'
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
  -e "COLLECTION=$collection" -e "LOGIN=${1-user}" -e "CATEGORY_ID=${2-}" -e "FROM=${3-}" -e "TO=${4-}" \
  mongodb mongosh --quiet --norc "$uri" --file /dev/stdin <<'MONGO'
const { COLLECTION, LOGIN, CATEGORY_ID, FROM, TO } = process.env;
const collection = db.getCollection(COLLECTION);
let categoryId, from, to;
if (!CATEGORY_ID && !FROM && !TO) {
  const [sample] = collection.find(
    { userId: LOGIN, categoryId: { $type: "number" }, createdAt: { $type: "date" } },
    { categoryId: 1, createdAt: 1 }
  ).limit(1).maxTimeMS(5000).toArray();
  if (!sample) throw new Error("В notifications нет уведомлений для " + LOGIN + ".");
  categoryId = sample.categoryId;
  from = new Date(sample.createdAt);
  from.setUTCHours(0, 0, 0, 0);
  to = new Date(from.getTime() + 86400000);
} else {
  from = new Date(FROM);
  to = new Date(TO);
  if (!LOGIN || !/^[1-9][0-9]*$/.test(CATEGORY_ID) || BigInt(CATEGORY_ID) > 9223372036854775807n || !(from < to)) {
    throw new Error("Укажи логин, ID категории и даты в порядке FROM TO.");
  }
  categoryId = NumberLong(CATEGORY_ID);
}

const filter = { userId: LOGIN, categoryId, createdAt: { $gte: from, $lt: to } };
const explain = hint => collection.find(filter, { actionLog: 0 })
  .sort({ createdAt: -1, _id: -1 }).limit(20).hint(hint).maxTimeMS(5000)
  .explain("executionStats").executionStats;

const indexed = explain("notifications_user_category_created_id");
const scanned = explain({ $natural: 1 });
const row = (label, left, right) => print(label.padEnd(24) + String(left).padStart(12) + String(right).padStart(12));

row("Метрика", "Без индекса", "С индексом");
row("Документов просмотрено", scanned.totalDocsExamined, indexed.totalDocsExamined);
row("Документов найдено", scanned.nReturned, indexed.nReturned);
row("Время, мс", scanned.executionTimeMillis, indexed.executionTimeMillis);
MONGO
