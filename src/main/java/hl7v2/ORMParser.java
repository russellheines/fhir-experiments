package hl7v2;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.hl7v2.util.Terser;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class ORMParser {

    private static final Logger log = LoggerFactory.getLogger(ORMParser.class);

    private final Terser terser;

    private Patient patient;
    private Encounter encounter;
    private Practitioner requester;
    private ServiceRequest order;

    public ORMParser(String message) throws Exception {

        HapiContext context = new DefaultHapiContext();
        Parser p = context.getGenericParser();
        Message hapiMsg = p.parse(message);

        // https://hapifhir.github.io/hapi-hl7v2/base/apidocs/ca/uhn/hl7v2/util/Terser.html
        terser = new Terser(hapiMsg);

        parsePatient();
        parseEncounter();
        parseRequester();
        parseServiceRequest();
    }

    private void parsePatient() throws Exception {

        if (terser.get("PID-3") != null) {

            patient = new Patient();
            patient.addIdentifier()
                    .setSystem("https://fhir.experiments.com/System/MeditechUrn/" + terser.get("MSH-4-2"))
                    .setValue(terser.get("PID-3-1"));
        }
    }

    private void parseEncounter() throws Exception {

        if (terser.get("PID-18") != null) {

            encounter = new Encounter();
            encounter.addIdentifier()
                    .setSystem("https://fhir.experiments.com/System/MeditechVisitNumber/" + terser.get("MSH-4-3"))
                    .setValue(terser.get("PID-18"));
        }
    }

    private void parseRequester() throws Exception {

        if (terser.get("OBR-16-1") != null) {

            requester = new Practitioner();
            requester.setId(terser.get("OBR-16-1"));

            if ((terser.get("OBR-16-2") != null) || (terser.get("OBR-16-3") != null)) {

                HumanName humanName = requester.addName();
                if (terser.get("OBR-16-2") != null) {
                    humanName.setFamily(terser.get("OBR-16-2"));
                }
                if (terser.get("OBR-16-3") != null) {
                    humanName.addGiven(terser.get("OBR-16-3"));
                }
            }
        }
    }

    private void parseServiceRequest() throws Exception {

        order = new ServiceRequest();
        order.setIntent(ServiceRequest.ServiceRequestIntent.ORDER);

        order.addIdentifier()
                .setSystem("https://fhir.experiments.com/System/MeditechUrn/" + terser.get("MSH-4-3"))
                .setValue(terser.get("OBR-2-1"));

        order.addIdentifier()
                .setSystem("https://fhir.experiments.com/System/MeditechOrderNumber/" + terser.get("MSH-4-3"))
                .setValue(terser.get("ORC-2"));

        order.setStatus(ServiceRequest.ServiceRequestStatus.UNKNOWN);
        order.addExtension()
                .setUrl("https://fhir.experiments.com/System/MeditechOrderStatus/" + terser.get("MSH-4-3"))
                .setValue(new StringType(terser.get("OBR-25")));

        try {
            Period period = new Period();
            period.setStart(new SimpleDateFormat("yyyyMMddHHmmss").parse(terser.get("OBR-7")));
            order.setOccurrence(period);

            order.setAuthoredOn(new SimpleDateFormat("yyyyMMddHHmmss").parse(terser.get("ORC-9")));
        }
        catch (ParseException e) {
            e.printStackTrace();
        }

        CodeableConcept codeConcept = new CodeableConcept();
        codeConcept.addCoding()
                .setSystem("https://fhir.experiments.com/System/MeditechOrderType/" + terser.get("MSH-4-3"))
                .setCode(terser.get("OBR-4-1"))
                .setDisplay(terser.get("OBR-4-2"));

        order.setCode(codeConcept);
    }

    public void prettyPrint() {
        FhirContext ctx = FhirContext.forR4();

        log.info(ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(patient));
        log.info(ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(encounter));
        log.info(ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(requester));
        log.info(ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(order));
    }

    public static void main(String[] args) throws Exception {

        String message = new String(Files.readAllBytes(Paths.get("./src/main/resources/ORM-1.hl7")), StandardCharsets.UTF_8);

        new ORMParser(message).prettyPrint();
   }
}