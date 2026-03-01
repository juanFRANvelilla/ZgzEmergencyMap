# Etapa 1: Construcción de la aplicación Angular
FROM cgr.dev/chainguard/node:latest AS build

WORKDIR /app

# Copiar solo los archivos de dependencias primero (para aprovechar el caché de Docker)
COPY --chown=node:node package.json package-lock.json ./

# Instalar las dependencias (incluye devDependencies para el build)
RUN npm ci

# Copiar el resto de los archivos del proyecto
COPY --chown=node:node . .

# Construir la aplicación Angular para producción
RUN npm run build

# Etapa 2: Configuración de Nginx
FROM nginx:alpine

USER root
RUN apk add --no-cache vim bash

USER nginx
# Copiar los archivos compilados desde la carpeta correcta
COPY --from=build /app/dist/emergency-map-angular /usr/share/nginx/html
COPY ./nginx.conf /etc/nginx/nginx.conf
COPY ./entrypoint.sh /docker-entrypoint.d/99-entrypoint.sh

USER root
RUN sed -i 's/\r$//' /docker-entrypoint.d/99-entrypoint.sh && \
    chmod +x /docker-entrypoint.d/99-entrypoint.sh
USER nginx

EXPOSE 8080

CMD ["/bin/bash", "-c", "/docker-entrypoint.d/99-entrypoint.sh && nginx -g 'daemon off;'"]
