#!/usr/bin/env bash
set -euo pipefail

runner="$(dirname -- "${BASH_SOURCE[0]}")/mongo_explain_comparsion.sh"
output="${MONGO_EXPLAIN_CSV:-mongo_explain_comparison.csv}"
temporary="$(mktemp -- "$output.XXXXXX")"
trap 'rm -f -- "$temporary"' EXIT

echo 'run,docs_examined_without_index,docs_examined_with_index,docs_returned_without_index,docs_returned_with_index,time_ms_without_index,time_ms_with_index' > "$temporary"

for run in {1..20}; do
  if ! bash "$runner" "$@" | awk -v run="$run" '
    /^Документов просмотрено[[:space:]]/ { scanned = pair(); scannedRows++ }
    /^Документов найдено[[:space:]]/ { returned = pair(); returnedRows++ }
    /^Время, мс[[:space:]]/ { elapsed = pair(); elapsedRows++ }

    function pair() {
      if ($(NF - 1) !~ /^[0-9]+$/ || $NF !~ /^[0-9]+$/) invalid = 1
      return $(NF - 1) "," $NF
    }

    END {
      if (invalid || scannedRows != 1 || returnedRows != 1 || elapsedRows != 1) exit 1
      print run "," scanned "," returned "," elapsed
    }
  ' >> "$temporary"; then
    echo "Замер $run из 20 не удался. CSV не обновлён." >&2
    exit 1
  fi
done

mv -fT -- "$temporary" "$output"
printf '20 замеров сохранены в %s\n' "$output"
