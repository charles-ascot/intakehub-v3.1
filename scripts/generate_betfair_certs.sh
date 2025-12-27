#!/bin/bash
set -e

echo "Generating Betfair Client Certificates..."

# 1. Generate Private Key
openssl genrsa -out client-2048.key 2048

# 2. Create Config for CSR
cat > openssl.cnf <<EOL
[ req ]
distinguished_name = req_distinguished_name
req_extensions = v3_req
prompt = no

[ req_distinguished_name ]
C = UK
ST = London
L = London
O = IntakeHub
OU = Betting
CN = intakehub-client

[ v3_req ]
basicConstraints = CA:FALSE
keyUsage = digitalSignature, keyEncipherment
extendedKeyUsage = clientAuth
EOL

# 3. Generate CSR
openssl req -new -config openssl.cnf -key client-2048.key -out client-2048.csr

# 4. Self-Sign Certificate (valid for 365 days)
openssl x509 -req -days 365 -in client-2048.csr -signkey client-2048.key -out client-2048.crt -extfile openssl.cnf -extensions v3_req

# 5. Create PEM bundle (optional, logical)
cat client-2048.crt client-2048.key > client-2048.pem

echo "------------------------------------------------"
echo "✅ Certificates Generated!"
echo "Files:"
echo "  - client-2048.key (Private Key - Keep Secure!)"
echo "  - client-2048.crt (Public Certificate)"
echo "------------------------------------------------"
echo "NEXT STEPS:"
echo "1. Log in to Betfair: https://www.betfair.com/exchange/plus/"
echo "2. Go to 'My Account' -> 'My Details' -> 'Security Settings' -> 'Automated Betting Program Access'"
echo "3. Upload 'client-2048.crt'"
echo "4. Use the contents of 'client-2048.crt' and 'client-2048.key' in the IntakeHub Dashboard credentials."
echo "------------------------------------------------"
