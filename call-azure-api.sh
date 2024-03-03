curl -i -X POST "https://c3po-demo-01.openai.azure.com/openai/deployments/gpt-4/chat/completions?api-version=2023-05-15" \
  -H "Content-Type: application/json" \
  -H "api-key: ${c3p0_api_key}" \
  -d \
'{
  "model":"gpt-3.5-turbo",
  "messages":[
    {
      "role":"system",
      "content":"You are C3P0, a protocol droid. You are fluent in over six million forms of communication."
    },
    {
      "role":"user",
      "content":"Greet the user."
    }
  ],
  "temperature":1.0,
  "top_p":1.0,
  "presence_penalty":0.0,
  "frequency_penalty":0.0
}'