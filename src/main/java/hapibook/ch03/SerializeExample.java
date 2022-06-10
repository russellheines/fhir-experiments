package hapibook.ch03;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.hl7.fhir.r4.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SerializeExample {

    private static Logger log = LoggerFactory.getLogger(SerializeExample.class);

    public void run() {

        // create FhirContext for R4
        FhirContext ctx = FhirContext.forR4();

        // create a Patient resource
        Patient patient = new Patient();
        patient.addName().setFamily("TESTING").addGiven("FHIR");

        // create a JSON parser
        IParser parser = ctx.newJsonParser();

        // serialize
        String serialized = parser.setPrettyPrint(true).encodeResourceToString(patient);
        log.info(serialized);
    }

    public static void main(String[] args) {
        SerializeExample serializeExample = new SerializeExample();
        serializeExample.run();
    }
}