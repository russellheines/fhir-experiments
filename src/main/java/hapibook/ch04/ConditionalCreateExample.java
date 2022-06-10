package hapibook.ch04;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConditionalCreateExample {

    private static Logger log = LoggerFactory.getLogger(ConditionalCreateExample.class);

    public void run() {

        // Create FhirContext for R4
        FhirContext ctx = FhirContext.forR4();

        // Create a generic client for http://localhost:8080/fhir/
        IGenericClient client = ctx.newRestfulGenericClient("http://localhost:8080/fhir/");

        Patient patient = new Patient();
        patient.addIdentifier().setValue("J516754").setSystem("https://fhir.experiments.com/PatientAlias/BACKENDID/HCA:KYA");
        patient.addName().setFamily("TESTING").addGiven("FHIR");

        // Create a bundle that will be used as a transaction
        Bundle bundle = new Bundle();
        bundle.setType(Bundle.BundleType.TRANSACTION);

        // Add the patient as an entry. This entry is a POST with an
        // If-None-Exist header (conditional create) meaning that it
        // will only be created if there isn't already a Patient with
        // the identifier
        bundle.addEntry()
                .setFullUrl(patient.getIdElement().getValue())
                .setResource(patient)
                .getRequest()
                .setUrl("Patient")
                .setIfNoneExist("identifier=https://fhir.experiments.com/PatientAlias/BACKENDID/HCA:KYA|J516754")
                .setMethod(Bundle.HTTPVerb.POST);

        // Log the request
        log.info(ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(bundle));

        Bundle response = client.transaction().withBundle(bundle).execute();

        // Log the response
        log.info(ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(response));
    }

    public static void main(String[] args) {
        ConditionalCreateExample conditionalCreateExample = new ConditionalCreateExample();
        conditionalCreateExample.run();
    }
}