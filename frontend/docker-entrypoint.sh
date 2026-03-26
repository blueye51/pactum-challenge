#!/bin/sh
echo "BACKEND_URL=${BACKEND_URL}"
echo "PORT=${PORT}"

if [ -z "$BACKEND_URL" ]; then
  echo "ERROR: BACKEND_URL is not set"
  exit 1
fi

if [ -z "$PORT" ]; then
  PORT=80
fi

envsubst '${BACKEND_URL} ${PORT}' < /etc/nginx/nginx.conf.template > /etc/nginx/conf.d/default.conf
cat /etc/nginx/conf.d/default.conf
exec nginx -g 'daemon off;'
