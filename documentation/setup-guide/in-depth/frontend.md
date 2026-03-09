# Frontend Deep Dive

This document explains the technical details of the Code Arena frontend, built with Angular and served via Nginx.

## Dockerfile Breakdown
The frontend also uses a multi-stage build.

### Stage 1: Build
- **`npm install -g npm@latest && npm install`**:
    - `npm install -g npm@latest`: This updates the `npm` package manager itself to the latest version. While modern Node.js images come with `npm` pre-installed, it is often a few versions behind. Updating it ensures you have the latest performance improvements, security patches, and bug fixes for the installation process. It's a "best practice" for stability, though not strictly required if the bundled version works for you.
    - `npm install`: Downloads the libraries needed for the project.
- **`npm run build -- --configuration production`**:
    - The `--` is the **End of Options** delimiter. 
    - It tells `npm` that it should stop parsing options for itself and pass all subsequent arguments directly to the underlying script being executed (in this case, the Angular `ng build` command).
    - Without it, `npm` might try to interpret `--configuration` as an option for the `npm run` command itself, which would result in an error or be ignored.

### Stage 2: Serving with Nginx
- **Dist Folder Structure**:
    - `# Angular 17+ uses 'browser' subfolder in 'dist'`: In recent versions (17+), Angular changed its output directory to support Server-Side Rendering (SSR) better. The static files for the browser are now placed in `dist/frontend/browser`.
- **`CMD ["nginx", "-g", "daemon off;"]`**:
    - **Yes**, this sets the foreground process.
    - By default, Nginx runs as a background "daemon". However, a Docker container exits as soon as its main process stops. Using `daemon off;` forces Nginx to stay in the foreground, keeping the container alive.

## Nginx Configuration (`frontend/nginx.conf`)
This is a lightweight configuration to serve the static Angular files.

- **`listen 80;`**: The internal port the container listens on.
- **`root /usr/share/nginx/html;`**: The directory where Nginx looks for the files (where we copied our `dist` files).
- **`location / { try_files $uri $uri/ /index.html; }`**:
    - This is critical for Single Page Applications (SPAs).
    - If a user refreshes the page on a route like `/profile`, Nginx would normally look for a file named `profile`. Since that file doesn't exist (Angular handles routing internally), `try_files` tells Nginx to serve `index.html` instead. Angular then takes over and displays the correct page.

## References
- [Angular Official Documentation](https://angular.io/docs)
- [Angular CLI Build Reference](https://angular.io/cli/build)
- [Nginx Official Documentation](https://nginx.org/en/docs/)
- [Nginx try_files Directive](https://nginx.org/en/docs/http/ngx_http_core_module.html#try_files)
