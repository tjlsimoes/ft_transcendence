FROM eclipse-temurin:21-jre-alpine
RUN apk add --no-cache nginx
COPY nginx.conf /etc/nginx/nginx.conf
EXPOSE 80 443
ENTRYPOINT []
CMD ["nginx", "-g", "daemon off;"]
