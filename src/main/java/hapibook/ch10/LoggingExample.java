package hapibook.ch10;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingExample {

    private static Logger log = LoggerFactory.getLogger(LoggingExample.class);

    public void run() {

        // create FhirContext for R4
        FhirContext ctx = FhirContext.forR4();

        // create a generic client for https://hapi.fhir.org/baseR4
        IGenericClient client = ctx.newRestfulGenericClient("http://localhost:8080/fhir/");

        // Create a logging interceptor
        LoggingInterceptor loggingInterceptor = new LoggingInterceptor();

        // Optionally you may configure the interceptor (by default only
        // summary info is logged)
        loggingInterceptor.setLogRequestSummary(true);  // true by default
        loggingInterceptor.setLogResponseSummary(true);  // true by default
        loggingInterceptor.setLogRequestHeaders(true);
        loggingInterceptor.setLogResponseHeaders(true);
        loggingInterceptor.setLogRequestBody(true);
        loggingInterceptor.setLogResponseBody(true);

        // Register the interceptor with your client
        client.registerInterceptor(loggingInterceptor);

        // copied from ConditionalUpdateExample

        // create a new Patient resource similar to the one in PatientExample but do NOT specify an ID
        Patient patient = new Patient();

        patient.addIdentifier().setValue("G854").setSystem("https://fhir.experiments.com/System/MeditechUrn/TNA");
        patient.addIdentifier().setValue("G000000852").setSystem("https://fhir.experiments.com/System/MeditechMrn/COCNV");

        patient.addName().setFamily("TESTING").addGiven("FHIR");

        // Create a bundle that will be used as a transaction
        Bundle bundle = new Bundle();
        bundle.setType(Bundle.BundleType.TRANSACTION);

        // Add the patient as an entry. This entry is a PUT with a
        // search parameter (conditional update)
        bundle.addEntry()
                .setFullUrl(patient.getIdElement().getValue())
                .setResource(patient)
                .getRequest()
                .setUrl("Patient?identifier=https://fhir.experiments.com/System/MeditechUrn/TNA|G854")
                //.setIfNoneExist("identifier=https://fhir.experiments.com/System/MeditechUrn/TNA|G844")
                .setMethod(Bundle.HTTPVerb.PUT);

        client.transaction().withBundle(bundle).execute();
    }

    public static void main(String[] args) {
        LoggingExample loggingExample = new LoggingExample();
        loggingExample.run();
    }
}