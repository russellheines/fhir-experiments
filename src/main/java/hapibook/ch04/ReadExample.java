package hapibook.ch04;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.r4.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadExample {

    private static Logger log = LoggerFactory.getLogger(ReadExample.class);

    public void run() {

        // create FhirContext for R4
        FhirContext ctx = FhirContext.forR4();

        // create a generic client for https://hapi.fhir.org/baseR4
        IGenericClient client = ctx.newRestfulGenericClient("https://hapi.fhir.org/baseR4");

        // read a Patient resource with id 1570412
        Patient patient = client.read().resource(Patient.class).withId("1570412").execute();

        // pretty print the response as JSON
        String serialized = ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(patient);
        log.info(serialized);
    }

    public static void main(String[] args) {
        ReadExample readExample = new ReadExample();
        readExample.run();
    }
}