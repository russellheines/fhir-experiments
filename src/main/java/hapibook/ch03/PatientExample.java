package hapibook.ch03;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class PatientExample {

    private static Logger log = LoggerFactory.getLogger(PatientExample.class);

    public void run() {

        // create FhirContext for R4
        FhirContext ctx = FhirContext.forR4();

        // create a Patient resource
        Patient patient = new Patient();
        patient.setId("COCNV-G000000852");

        patient.addIdentifier().setValue("G844").setSystem("https://fhir.experiments.com/System/MeditechUrn/TNA");
        patient.addIdentifier().setValue("G000000852").setSystem("https://fhir.experiments.com/System/MeditechMrn/COCNV");

        patient.addName().setFamily("TESTING").addGiven("FHIR");

        patient.setGender(Enumerations.AdministrativeGender.valueOf("FEMALE"));

        try {
            patient.setBirthDate(new SimpleDateFormat("yyyyMMdd").parse("19590508"));
        }
        catch (ParseException e) {
            e.printStackTrace();
        }

        // pretty print
        String serialized = ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(patient);
        System.out.println(serialized);
    }

    public static void main (String[] args) {
        PatientExample patientExample = new PatientExample();
        patientExample.run();
    }
}