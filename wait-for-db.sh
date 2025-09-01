#!/bin/sh
# wait-for-db.sh

set -e

host="$1"
shift
cmd="$@"

# Wait until the database is available
until PGPASSWORD=$SPRING_DATASOURCE_PASSWORD psql -h "$host" -U "jira" -d "jira" -c 'SELECT 1' > /dev/null 2>&1; do
  >&2 echo "Postgres is unavailable - sleeping"
  sleep 2
done

# Additional delay to ensure initialization scripts complete
>&2 echo "Postgres is up - waiting for initialization scripts to complete"
sleep 15

>&2 echo "Database is ready - executing command"
exec $cmd