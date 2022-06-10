package hapibook.ch03;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.hl7.fhir.r4.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeserializeExample {

    private static Logger log = LoggerFactory.getLogger(DeserializeExample.class);

    public void run() {

        // create FhirContext for R4
        FhirContext ctx = FhirContext.forR4();

        // create serialized Patient resource
        String json = "{" +
                "\"resourceType\" : \"Patient\"," +
                "  \"name\" : [{" +
                "    \"family\": \"TESTING\"" +
                "  }]" +
                "}";

        // create a JSON parser
        IParser parser = ctx.newJsonParser();

        // deserialize
        String lastName = parser.parseResource(Patient.class, json).getName().get(0).getFamily();
        log.info(lastName);
    }

    public static void main(String[] args) {
        DeserializeExample deserializeExample = new DeserializeExample();
        deserializeExample.run();
    }
}