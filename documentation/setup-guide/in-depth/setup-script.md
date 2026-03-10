# Setup Script Deep Dive

This document explains the logic behind `setup.sh`, the automated configuration utility for Code Arena.

## Configuration Logic (Prompting for Variables)

The script parses `.env.example` to ensure the presence of all required variables.

### The File Reading Loop
```bash
while IFS= read -u 3 -r line || [[ -n "$line" ]]; do
  ...
done 3< "$ENV_EXAMPLE"
```
- **`3< "$ENV_EXAMPLE"`**: This connects the `.env.example` file to "File Descriptor 3".
- **`read -u 3`**: Tells the `read` command to read from descriptor 3 instead of standard input (keyboard).
- **`|| [[ -n "$line" ]]`**: Ensures the last line of the file is processed even if it doesn't end with a newline character.

### Filtering and Logic
- **`if [[ -z "$line" ]] || [[ "$line" == \#* ]]; then`**:
    - Skips empty lines (`-z "$line"`) or lines starting with a hash (`#`), which are comments.
- **`var_name=$(echo "$line" | cut -d'=' -f1)`**:
    - Takes a line like `DB_PASSWORD=secret` and "cuts" it at the `=` sign, taking the first part (`DB_PASSWORD`).
- **`if grep -q "^$var_name=" "$temp_env"; then`**:
    - Checks if the variable name already exists at the start of a line in the configuration file.
- **`current_val=$(grep "^$var_name=" "$temp_env" | cut -d'=' -f2-)`**:
    - Finds the line for that variable and extracts everything *after* the first `=` sign.

## SSL Certificate Generation

```bash
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
  -keyout "$SSL_DIR/localhost.key" \
  -out "$SSL_DIR/localhost.crt" \
  -subj "/C=FR/ST=Paris/L=Paris/O=42/OU=Transcendence/CN=localhost"
```

- **`req -x509`**: Creates a self-signed certificate (instead of a certificate request).
- **`-noenc`**: (Replaces deprecated `-nodes`) Means the private key won't be encrypted with a password. This allows Nginx to start automatically without human intervention.
- **`-days 365`**: The certificate is valid for one year.
- **`-newkey rsa:2048`**: Generates a new 2048-bit RSA private key along with the certificate.
- **`-keyout` / `-out`**: Where to save the private key and the certificate file.
- **`-subj`**: Sets the certificate information directly via the command line. (Default: Lisbon, Portugal for 42).

## References
- [OpenSSL req Documentation](https://www.openssl.org/docs/manmaster/man1/openssl-req.html)
- [OpenSSL x509 Documentation](https://www.openssl.org/docs/manmaster/man1/openssl-x509.html)
