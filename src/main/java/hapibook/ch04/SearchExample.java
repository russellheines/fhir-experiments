package hapibook.ch04;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SearchExample {

    private static Logger log = LoggerFactory.getLogger(SearchExample.class);

    public void run() {

        // create FhirContext for R4
        FhirContext ctx = FhirContext.forR4();

        // create a generic client for https://hapi.fhir.org/baseR4
        IGenericClient client = ctx.newRestfulGenericClient("https://hapi.fhir.org/baseR4");

        // search for Patients with a name that matches "Isobel140"
        Bundle bundle = client.search()
                .forResource(Patient.class)
                .where(Patient.NAME.matches().value("Isobel140"))
                .returnBundle(Bundle.class)
                .execute();

        // pretty print the response as JSON
        String serialized = ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(bundle);
        log.info(serialized);
    }

    public static void main(String[] args) {
        SearchExample searchExample = new SearchExample();
        searchExample.run();
    }
}