FROM node:22-alpine AS build

WORKDIR /workspace

COPY package.json package-lock.json ./
RUN npm ci

COPY . .

ARG VITE_API_BASE_URL=/api
ARG VITE_DASHBOARD_REFRESH_MS=15000
ARG VITE_ALERT_BADGE_REFRESH_MS=15000

ENV VITE_API_BASE_URL=${VITE_API_BASE_URL}
ENV VITE_DASHBOARD_REFRESH_MS=${VITE_DASHBOARD_REFRESH_MS}
ENV VITE_ALERT_BADGE_REFRESH_MS=${VITE_ALERT_BADGE_REFRESH_MS}

RUN npm run build

FROM nginx:1.29-alpine

COPY nginx/default.conf /etc/nginx/conf.d/default.conf
COPY --from=build /workspace/dist /usr/share/nginx/html

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
    CMD wget -qO- http://127.0.0.1/healthz || exit 1
