#!/bin/bash
# Recreate env.js from environment variables at runtime

# Define the path to the env.js file inside the nginx html directory
ENV_JS_FILE="/usr/share/nginx/html/assets/env.js"

# Create or overwrite the file with the basic object structure
echo "window.__env = {" > $ENV_JS_FILE

# Iterate through all environment variables
while IFS='=' read -r key value; do
  # Exclude system environment variables or bash special vars if needed
  # but here we'll just write them all, or you could filter by a prefix
  
  # Properly escape double quotes in the value
  escaped_value=$(echo "$value" | sed 's/"/\\"/g')
  
  # Append the key-value pair to the env.js file
  echo "  \"$key\": \"$escaped_value\"," >> $ENV_JS_FILE
done < <(env)

# Close the JSON object structure
echo "};" >> $ENV_JS_FILE

# Make sure permissions are correct so Nginx can read it
chmod 644 $ENV_JS_FILE

echo "Generated $ENV_JS_FILE successfully from current environment."
