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

sed "s|NGINX_BACKEND|${BACKEND_URL}|g; s|NGINX_PORT|${PORT}|g" \
  /etc/nginx/nginx.conf.template > /etc/nginx/conf.d/default.conf

echo "--- generated nginx config ---"
cat /etc/nginx/conf.d/default.conf
echo "--- end config ---"

exec nginx -g 'daemon off;'
