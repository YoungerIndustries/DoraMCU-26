#!/bin/bash
cd /tmp/migration
for f in $(ls -1 *.sql | sort -V); do
    echo "Applying $f"
    mysql -uroot -pdoramcu-prod-2026-secure doramcu < "$f" 2>&1
    if [ $? -ne 0 ]; then
        echo "FAILED: $f"
        exit 1
    fi
done
echo "All migrations applied successfully"
