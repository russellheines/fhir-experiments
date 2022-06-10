## About

FHIR-related experiments, including:

* Examples of using HAPI FHIR (https://hapifhir.io/hapi-fhir/docs/)
* Examples of using SMART JS Client Library (http://docs.smarthealthit.org/client-js/)

## Running a HAPI FHIR server locally

To start a HAPI FHIR server locally without any data:

```bash
docker pull hapiproject/hapi:latest
docker run -p 8080:8080 hapiproject/hapi:latest
```

For more information see https://github.com/hapifhir/hapi-fhir-jpaserver-starter/.