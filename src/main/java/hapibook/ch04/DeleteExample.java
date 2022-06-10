package hapibook.ch04;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteExample {

    private static Logger log = LoggerFactory.getLogger(DeleteExample.class);

    public void run() {

        // create FhirContext for R4
        FhirContext ctx = FhirContext.forR4();

        // create a generic client for http://localhost:8080/fhir/
        IGenericClient client = ctx.newRestfulGenericClient("http://localhost:8080/fhir/");

        MethodOutcome response = client
                .delete()
                .resourceById(new IdType("Patient", "1"))
                .execute();

        // outcome may be null if the server didn't return one
        OperationOutcome outcome = (OperationOutcome) response.getOperationOutcome();
        if (outcome != null) {
            log.info(outcome.getIssueFirstRep().getDetails().getCodingFirstRep().getCode());
        }
    }

    public static void main(String[] args) {
        DeleteExample deleteExample = new DeleteExample();
        deleteExample.run();
    }
}