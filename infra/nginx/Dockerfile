FROM nginx:1.28.2-alpine3.23-slim
COPY loading.html /usr/share/nginx/html/loading.html
COPY loading.css /usr/share/nginx/html/loading.css
COPY loading.js /usr/share/nginx/html/loading.js
CMD ["nginx", "-g", "daemon off;"]
