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
FROM cgr.dev/chainguard/nginx:latest

# Copiar los configuraciones de nginx
COPY ./nginx.conf /etc/nginx/nginx.conf

# Copiar los archivos compilados desde la carpeta correcta
COPY --from=build /app/dist/emergency-map-angular /usr/share/nginx/html

EXPOSE 8080

CMD ["nginx", "-g", "daemon off;"]
