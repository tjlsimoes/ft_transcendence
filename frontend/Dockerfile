# Stage 1: Build the Angular application
FROM node:24-alpine AS build
WORKDIR /app
COPY package*.json ./
# Update npm to avoid version mismatch notices during build
RUN npm install -g npm@latest && npm install
COPY . .
RUN npm run build -- --configuration production

# Stage 2: Serve the application using Nginx
FROM nginx:1.28.2-alpine3.23
COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html
# Angular 17+ uses 'browser' subfolder in 'dist'
# If it's an older version, it might be just /app/dist/frontend
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
