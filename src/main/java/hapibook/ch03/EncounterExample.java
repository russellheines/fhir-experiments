package hapibook.ch03;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class EncounterExample {

    private static Logger log = LoggerFactory.getLogger(EncounterExample.class);

    public void run() {

        // create FhirContext for R4
        FhirContext ctx = FhirContext.forR4();

        // create an Encounter resource
        Encounter encounter = new Encounter();
        encounter.setId("COCNV-G00000010001");

        encounter.addIdentifier().setValue("G00000010001").setSystem("https://fhir.experiments.com/System/MeditechVisitNumber/COCNV");

        encounter.setClass_(new Coding().setCode("AMB").setSystem("http://terminology.hl7.org/CodeSystem/v3-ActCode"));

        CodeableConcept type = encounter.addType();
        type.addCoding().setCode("DEP").setSystem("https://fhir.experiments.com/System/MeditechAccountStatus");
        type.addCoding().setCode("REF").setSystem("https://fhir.experiments.com/System/MeditechPatientType");

        encounter.setSubject(new Reference("Patient/COCNV-G000000852"));

        Encounter.EncounterParticipantComponent attending = encounter.addParticipant();
        attending.addType().addCoding().setCode("ATND").setSystem("http://hl7.org/fhir/participant-type");

        try {
            Period period = new Period();
            period.setStart(new SimpleDateFormat("yyyyMMddhhmmss").parse("20190828143800"));
            period.setEnd(new SimpleDateFormat("yyyyMMddhhmmss").parse("20190828143800"));
            encounter.setPeriod(period);
        }
        catch (ParseException e) {
            e.printStackTrace();
        }

        encounter.addLocation(new Encounter.EncounterLocationComponent(new Reference("Location/COCNV")));

        // pretty print
        String serialized = ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(encounter);
        log.info(serialized);
    }

    public static void main (String[] args) {
        EncounterExample encounterExample = new EncounterExample();
        encounterExample.run();
    }
}