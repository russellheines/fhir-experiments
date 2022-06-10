package hapibook.ch04;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.r4.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateExample {

    private static Logger log = LoggerFactory.getLogger(CreateExample.class);

    public void run() {

        // create FhirContext for R4
        FhirContext ctx = FhirContext.forR4();

        // create a generic client for http://localhost:8080/fhir/
        IGenericClient client = ctx.newRestfulGenericClient("http://localhost:8080/fhir/");

        // create a new Patient resource similar to the one in PatientExample but do NOT specify an ID
        Patient patient = new Patient();

        patient.addIdentifier().setValue("G844").setSystem("https://fhir.experiments.com/System/MeditechUrn/TNA");
        patient.addIdentifier().setValue("G000000852").setSystem("https://fhir.experiments.com/System/MeditechMrn/COCNV");

        patient.addName().setFamily("TESTING").addGiven("FHIR");

        // push the new Patient resource to the server
        MethodOutcome outcome = client.create().resource(patient).execute();

        // print "Created id: <id>" from the response back from the server
        log.info("Created id: " + outcome.getId().getIdPart());
    }

    public static void main(String[] args) {
        CreateExample createExample = new CreateExample();
        createExample.run();
    }
}