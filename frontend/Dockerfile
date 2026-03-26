# Build stage
FROM node:22-alpine AS build
WORKDIR /app

COPY package.json package-lock.json* ./
RUN npm install

COPY . .
RUN npm run build

# Runtime stage — serve with nginx
FROM nginx:alpine

RUN rm /etc/nginx/conf.d/default.conf

COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf
COPY nginx.conf.template /etc/nginx/nginx.conf.template
COPY docker-entrypoint.sh /docker-entrypoint.sh
RUN chmod +x /docker-entrypoint.sh

# For local docker-compose: defaults to backend:8080, port 80
# For Railway: set BACKEND_URL and PORT via env vars
ENV BACKEND_URL=http://backend:8080
ENV PORT=80

EXPOSE 80
CMD ["/docker-entrypoint.sh"]
