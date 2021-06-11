# analyzer

Analyzer is a small web wrapper around Apache Tika. It has three endpoints you can use:

## / - use for health checks
A call to `GET /` should always return a 200 - ok response if the service is healthy.

## /upload
You can upload a file to `POST /upload`. The response will be a json payload containing the parsed information from the file.

With curl:
`curl -X POST -H "content-type: multipart/form-data" https://analyzer.surfpol.nl/upload -F "file=@<Insert file here>"`

## /analyze
You can also request analyzer to analyze a url using `POST /analyze`. You should send a json payload in the body pointing to the url. For example:

```json
{"url": "https://www.youtube.com/watch?v=kGlVcSMgtV4"}
```

With curl:
`curl --location --request POST 'localhost:8080/analyze' \
--header 'Content-Type: application/json' \
--data-raw '{"url": "https://www.youtube.com/watch?v=13cmHf_kt-Q"}'`
