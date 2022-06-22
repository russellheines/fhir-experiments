package hl7v2;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
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

public class ADTParser {

    private static final Logger log = LoggerFactory.getLogger(ADTParser.class);

    private final Terser terser;

    private Location facility;
    private Patient patient;
    private Encounter encounter;
    private Practitioner attending;

    public ADTParser(String message) throws Exception {

        HapiContext context = new DefaultHapiContext();
        Parser p = context.getGenericParser();
        Message hapiMsg = p.parse(message);

        // https://hapifhir.github.io/hapi-hl7v2/base/apidocs/ca/uhn/hl7v2/util/Terser.html
        terser = new Terser(hapiMsg);

        parseFacility();
        parsePatient();
        parseEncounter();
        parseAttending();
    }

    private void parseFacility() throws Exception {

        if (terser.get("MSH-4") != null) {

            facility = new Location();
            facility.setId(terser.get("MSH-4"));

            CodeableConcept facilityConcept = new CodeableConcept();
            facilityConcept.addCoding().setCode("bu").setSystem("http://terminology.hl7.org/CodeSystem/location-physical-type");
            facility.setPhysicalType(facilityConcept);
        }
    }

    private void parsePatient() throws Exception {

        patient = new Patient();

        if (terser.get("PID-3") != null) {
            patient.addIdentifier()
                    .setSystem("https://fhir.experiments.com/System/MeditechMrn/" + terser.get("MSH-4"))
                    .setValue(terser.get("PID-3-1"));
        }

        if (terser.get("PID-4") != null) {
            patient.addIdentifier()
                    .setSystem("https://fhir.experiments.com/System/MeditechUrn/" + terser.get("MSH-4"))
                    .setValue(terser.get("PID-4-1"));
        }

        if (terser.get("PID-5-1") != null || terser.get("PID-5-2") != null || terser.get("PID-5-3") != null) {
            HumanName name = patient.addName();
            name.setUse(HumanName.NameUse.OFFICIAL);
            if (terser.get("PID-5-1") != null) {
                name.setFamily(terser.get("PID-5-1"));
            }
            if (terser.get("PID-5-2") != null) {
                name.addGiven(terser.get("PID-5-2"));
            }
            if (terser.get("PID-5-3") != null) {
                name.addGiven(terser.get("PID-5-3"));
            }
        }

        if (terser.get("PID-8") != null) {
            if ("F".equalsIgnoreCase(terser.get("PID-8"))) {
                patient.setGender(Enumerations.AdministrativeGender.FEMALE);
            }
            else if ("M".equalsIgnoreCase(terser.get("PID-8"))) {
                patient.setGender(Enumerations.AdministrativeGender.MALE);
            }
            else {
                patient.setGender(Enumerations.AdministrativeGender.UNKNOWN);
            }
        }

        try {
            if (terser.get("PID-7") != null) {
                patient.setBirthDate(new SimpleDateFormat("yyyyMMdd").parse(terser.get("PID-7").substring(0, 8)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (terser.get("PID-13") != null) {
            patient.addTelecom()
                    .setSystem(ContactPoint.ContactPointSystem.PHONE)
                    .setUse(ContactPoint.ContactPointUse.HOME)
                    .setValue(terser.get("PID-13"));
        }

        if (terser.get("PID-11-1") != null || terser.get("PID-11-2") != null || terser.get("PID-11-3") != null
                || terser.get("PID-11-4") != null || terser.get("PID-11-5") != null) {
            Address address = patient.addAddress();
            address.setUse(Address.AddressUse.HOME);
            if (terser.get("PID-11-1") != null) {
                address.addLine(terser.get("PID-11-1"));
            }
            if (terser.get("PID-11-2") != null) {
                address.addLine(terser.get("PID-11-2"));
            }
            if (terser.get("PID-11-3") != null) {
                address.setCity(terser.get("PID-11-3"));
            }
            if (terser.get("PID-11-4") != null) {
                address.setState(terser.get("PID-11-4"));
            }
            if (terser.get("PID-11-5") != null) {
                address.setPostalCode(terser.get("PID-11-5"));
            }
        }

        try {
            Patient.ContactComponent nok = patient.addContact();
            if (terser.get("NK1-3") != null) {
                CodeableConcept nokRelationship = nok.addRelationship();
                nokRelationship.addCoding().setCode("N").setSystem("http://terminology.hl7.org/CodeSystem/v2-0131");
            }

            if (terser.get("NK1-2") != null) {
                HumanName nokName = new HumanName();
                nokName.setUse(HumanName.NameUse.OFFICIAL);
                nokName.setFamily(terser.get("NK1-2-1"));
                nokName.addGiven(terser.get("NK1-2-2"));
                nok.setName(nokName);
            }

            if (terser.get("NK1-4-1") != null || terser.get("NK1-4-2") != null || terser.get("NK1-4-3") != null
                    || terser.get("NK1-4-4") != null || terser.get("NK1-4-5") != null) {
                Address nokAddress = new Address();
                if (terser.get("NK1-4-1") != null) {
                    nokAddress.addLine(terser.get("NK1-4-1"));
                }
                if (terser.get("NK1-4-2") != null) {
                    nokAddress.addLine(terser.get("NK1-4-2"));
                }
                if (terser.get("NK1-4-3") != null) {
                    nokAddress.setCity(terser.get("NK1-4-3"));
                }
                if (terser.get("NK1-4-4") != null) {
                    nokAddress.setState(terser.get("NK1-4-4"));
                }
                if (terser.get("NK1-4-5") != null) {
                    nokAddress.setPostalCode(terser.get("NK1-4-5"));
                }
                nok.setAddress(nokAddress);
            }

            if (terser.get("NK1-5") != null) {
                nok.addTelecom()
                        .setSystem(ContactPoint.ContactPointSystem.PHONE)
                        .setUse(ContactPoint.ContactPointUse.HOME)
                        .setValue(terser.get("NK1-5"));
            }
        } catch (HL7Exception e) {
            log.info("HL7 does not contain next of kin.");
        }
    }

    private void parseEncounter() throws Exception {

        if (terser.get("PID-18") != null) {

            encounter = new Encounter();
            encounter.addIdentifier()
                    .setSystem("https://fhir.experiments.com/System/MeditechVisitNumber/" + terser.get("MSH-4"))
                    .setValue(terser.get("PID-18"));

            if (terser.get("PV1-18") != null) {
                if (("ADM".equalsIgnoreCase(terser.get("PV1-18"))) || ("REG".equalsIgnoreCase(terser.get("PV1-18")))) {
                    encounter.setStatus(Encounter.EncounterStatus.INPROGRESS);
                }
                if (("DIS".equalsIgnoreCase(terser.get("PV1-18"))) || ("DEP".equalsIgnoreCase(terser.get("PV1-18")))) {
                    encounter.setStatus(Encounter.EncounterStatus.FINISHED);
                }
                else if ("CAN".equalsIgnoreCase(terser.get("PV1-18"))) {
                    encounter.setStatus(Encounter.EncounterStatus.CANCELLED);
                }
            }

            if (terser.get("PV1-2") != null) {
                Coding classCoding = new Coding();
                classCoding.setSystem("http://terminology.hl7.org/CodeSystem/v3-ActCode");
                if (terser.get("PV1-2").equalsIgnoreCase("I")) {
                    classCoding.setCode("IMP");
                }
                else if (terser.get("PV1-2").equalsIgnoreCase("E")) {
                    classCoding.setCode("EMER");
                }
                else if (terser.get("PV1-2").equalsIgnoreCase("O")) {
                    classCoding.setCode("AMB");
                }
                encounter.setClass_(classCoding);
            }

            if ((terser.get("PV1-18") != null) && (terser.get("PV1-41") != null)) {
                CodeableConcept type = new CodeableConcept();
                type.addCoding().setCode(terser.get("PV1-41"))
                        .setSystem("https://fhir.experiments.com/System/MeditechAccountStatus");
                type.addCoding().setCode(terser.get("PV1-18"))
                        .setSystem("https://fhir.experiments.com/System/MeditechPatientType");
                encounter.addType(type);
            }

            if (terser.get("PV1-10") != null) {
                CodeableConcept serviceTypeConcept = new CodeableConcept();
                serviceTypeConcept.addCoding().setCode(terser.get("PV1-10"))
                        .setSystem("https://fhir.experiments.com/MedicalServiceType");
                encounter.setServiceType(serviceTypeConcept);
            }

            if (terser.get("PV2-3") != null)
                encounter.addReasonCode().setText(terser.get("PV2-3"));

            if (terser.get("PV1-44") != null || terser.get("PV1-45") != null) {
                try {
                    Period period = new Period();
                    if (terser.get("PV1-44") != null) {
                        period.setStart(new SimpleDateFormat("yyyyMMddHHmm").parse(terser.get("PV1-44")));
                    }
                    if (terser.get("PV1-45") != null) {
                        period.setEnd(new SimpleDateFormat("yyyyMMddHHmm").parse(terser.get("PV1-45")));
                    }
                    encounter.setPeriod(period);
                }
                catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void parseAttending() throws Exception {

        if (terser.get("PV1-7-1") != null) {

            attending = new Practitioner();
            attending.setId(terser.get("PV1-7-1"));

            if ((terser.get("PV1-7-2") != null) || (terser.get("PV1-7-3") != null)) {

                HumanName humanName = attending.addName();
                if (terser.get("PV1-7-2") != null) {
                    humanName.setFamily(terser.get("PV1-7-2"));
                }
                if (terser.get("PV1-7-3") != null) {
                    humanName.addGiven(terser.get("PV1-7-3"));
                }
            }
        }
    }

    public void prettyPrint() {
        FhirContext ctx = FhirContext.forR4();

        log.info(ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(facility));
        log.info(ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(patient));
        log.info(ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(encounter));
        log.info(ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(attending));
    }

    public static void main(String[] args) throws Exception {

        String message = new String(Files.readAllBytes(Paths.get("./src/main/resources/ADT-1.hl7")), StandardCharsets.UTF_8);

        new ADTParser(message).prettyPrint();
    }
}