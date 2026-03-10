# Stage 1: Build the Angular application
FROM node:24-alpine AS build
WORKDIR /app
COPY package*.json ./
# Update npm to avoid version mismatch notices during build
RUN npm install -g npm@latest && npm install
COPY . .
RUN npm run build -- --configuration production

# Stage 2: Serve the application using Nginx
FROM nginx:1.28.2-alpine3.23-slim
WORKDIR /usr/share/nginx/html
COPY --from=build /app/dist/frontend/browser .
CMD ["nginx", "-g", "daemon off;"]
