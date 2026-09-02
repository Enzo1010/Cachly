# Etapa 1: Build do Angular
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# Etapa 2: Servidor Nginx
FROM nginx:alpine
# Remove a configuracao padrao do Nginx
RUN rm /etc/nginx/conf.d/default.conf
# Copia a configuracao customizada
COPY nginx.conf /etc/nginx/conf.d/default.conf
# Copia os arquivos da SPA compilados (Ajustado para o novo build system do Angular 17+)
COPY --from=build /app/dist/cachly-frontend/browser /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
