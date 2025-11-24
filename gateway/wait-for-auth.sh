#!/bin/sh
# wait-for-auth.sh – wait until the auth server's OpenID configuration endpoint is reachable

AUTH_URL="http://auth-server:9000/auth/.well-known/openid-configuration"
echo "Waiting for Auth server at $AUTH_URL ..."

# Loop until curl succeeds (exit code 0) with HTTP 2xx
until curl --silent --fail --output /dev/null "$AUTH_URL"; do
    echo "Auth server not yet available. Retrying in 5 seconds..."
    sleep 5
done

echo "Auth server is up – starting gateway application."
# Execute the gateway command (passed as arguments to this script)
exec "$@"
