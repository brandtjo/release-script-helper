#!/usr/bin/env bash
# Generate new RSA signing keys for JetBrains plugin publishing.
# Outputs base64-encoded values ready to paste into GitHub Secrets.

set -euo pipefail

KEYSTORE_PASSWORD="${KEYSTORE_PASSWORD:-$(openssl rand -base64 16)}"
OUTPUT_DIR="${OUTPUT_DIR:-.signing}"

mkdir -p "$OUTPUT_DIR"

echo "=== Generating 4096-bit RSA key pair ==="
openssl genpkey \
  -aes-256-cbc \
  -algorithm RSA \
  -out "$OUTPUT_DIR/private_encrypted.pem" \
  -pkeyopt rsa_keygen_bits:4096 \
  -pass "pass:$KEYSTORE_PASSWORD"

echo "=== Extracting unencrypted private key ==="
openssl rsa \
  -in "$OUTPUT_DIR/private_encrypted.pem" \
  -out "$OUTPUT_DIR/private.pem" \
  -passin "pass:$KEYSTORE_PASSWORD"

echo "=== Generating self-signed certificate (valid 365 days) ==="
openssl req \
  -key "$OUTPUT_DIR/private.pem" \
  -new \
  -x509 \
  -days 365 \
  -out "$OUTPUT_DIR/chain.crt" \
  -subj "/CN=Johannes Brandt/O=release-script-helper/L=Unknown/ST=Unknown/C=US"

echo ""
echo "=== Files generated in $OUTPUT_DIR/ ==="
echo "  private.pem          - unencrypted private key (PEM)"
echo "  private_encrypted.pem - encrypted private key (keep safe)"
echo "  chain.crt            - self-signed certificate chain"
echo ""
echo "=== Base64-encoded values for GitHub Secrets ==="
echo ""
echo "PRIVATE_KEY_PASSWORD:"
echo "$KEYSTORE_PASSWORD"
echo ""
echo "PRIVATE_KEY (base64 of private.pem):"
base64 -w 0 "$OUTPUT_DIR/private.pem"
echo ""
echo ""
echo "CERTIFICATE_CHAIN (base64 of chain.crt):"
base64 -w 0 "$OUTPUT_DIR/chain.crt"
echo ""
echo ""
echo "=== Done. Update these GitHub Secrets ==="
echo "  PUBLISH_TOKEN"
echo "  CERTIFICATE_CHAIN  -> base64 of $OUTPUT_DIR/chain.crt"
echo "  PRIVATE_KEY        -> base64 of $OUTPUT_DIR/private.pem"
echo "  PRIVATE_KEY_PASSWORD -> $KEYSTORE_PASSWORD"
echo ""
echo "=== To verify the key and cert match ==="
echo "  openssl x509 -in $OUTPUT_DIR/chain.crt -noout -modulus | md5sum"
echo "  openssl rsa -in $OUTPUT_DIR/private.pem -noout -modulus | md5sum"
echo "  (Both md5sums should be identical)"
