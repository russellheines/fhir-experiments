package hapibook.ch04;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.r4.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateExample {

    private static Logger log = LoggerFactory.getLogger(UpdateExample.class);

    public void run() {

        // create FhirContext for R4
        FhirContext ctx = FhirContext.forR4();

        // create a generic client for http://localhost:8080/fhir/
        IGenericClient client = ctx.newRestfulGenericClient("http://localhost:8080/fhir/");

        // read the Patient resource that you created in CreateExample
        Patient patient = client.read().resource(Patient.class).withId("1").execute();

        // change the patient's name
        patient.getName().get(0).setFamily("TESTING2");

        // push the updated Patient resource to the server
        MethodOutcome outcome = client.update().resource(patient).execute();

        // print "Updated id: <id>" from the response back from the server
        log.info("Updated id: " + outcome.getId().getIdPart());
    }

    public static void main(String[] args) {
        UpdateExample createExample = new UpdateExample();
        createExample.run();
    }
}