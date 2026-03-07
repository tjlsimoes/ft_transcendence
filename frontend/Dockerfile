# Stage 1: Build the Angular application
FROM eclipse-temurin:21-jre-alpine AS build
RUN apk add --no-cache nodejs npm
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build -- --configuration production

# Stage 2: Serve the application using Nginx
FROM eclipse-temurin:21-jre-alpine
RUN apk add --no-cache nginx
COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html
COPY nginx.conf /etc/nginx/http.d/default.conf
EXPOSE 80
ENTRYPOINT []
CMD ["nginx", "-g", "daemon off;"]
