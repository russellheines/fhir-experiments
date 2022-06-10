package hapibook.ch04;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConditionalUpdateExample {

    private static Logger log = LoggerFactory.getLogger(ConditionalUpdateExample.class);

    public void run() {

        // Create FhirContext for R4
        FhirContext ctx = FhirContext.forR4();

        // Create a generic client for http://localhost:8080/fhir/
        IGenericClient client = ctx.newRestfulGenericClient("http://localhost:8080/fhir/");

        Patient patient = new Patient();
        patient.addIdentifier().setValue("J516754").setSystem("https://fhir.experiments.com/PatientAlias/BACKENDID/HCA:KYA");
        patient.addName().setFamily("TESTING").addGiven("FHIR").addGiven("TWO");

        Location facility = new Location();
        facility.addIdentifier().setValue("COCQA1A").setSystem("https://fhir.experiments.com/LocationType/FACILITY");

        Location unit = new Location();
        unit.addIdentifier().setValue("J.SOLIS").setSystem("https://fhir.experiments.com/LocationType/UNIT");

        Practitioner attending = new Practitioner();
        attending.addIdentifier().setValue("PROVIDER.SIALBJA").setSystem("https://fhir.experiments.com/PersonAlias/BACKENDID/HCA:KYA");

        Encounter encounter = new Encounter();
        encounter.addIdentifier().setValue("J000440201:J516754:20190826.0932:REF:J00021227198").setSystem("https://fhir.experiments.com/EncounterAlias/BACKENDID/HCA:KYA:COCQA1A");

        // Option #1: Logical URIs

        // https://www.hl7.org/fhir/references.html
        // Note that in a bundle during a transaction, reference URLs may actually contain logical URIs (e.g. OIDs or UUIDSs) that resolve within the transaction. When processing the transaction, the server replaces the logical URL with what is the correct literal URL at the completion of the transaction.

        // NOTE: With HAPI, this only works in transactions (not batches)

        patient.setId(IdType.newRandomUuid());
        facility.setId(IdType.newRandomUuid());
        unit.setId(IdType.newRandomUuid());
        attending.setId(IdType.newRandomUuid());

        encounter.setSubject(new Reference(patient.getId()));
        encounter.addLocation().setLocation(new Reference(facility.getId()));
        encounter.addLocation().setLocation(new Reference(unit.getId()));
        encounter.addParticipant().setIndividual(new Reference(attending.getId()));

        // Option #2: Conditional references

        // https://www.hl7.org/fhir/http.html
        // In a transaction (and only in a transaction), references to resources may be replaced by a search URI that describes how to find the correct reference.

        // NOTE: With HAPI, this works in a transaction and in a batch - as long as the referenced resource already exists

        //encounter.setSubject(new Reference("Patient?identifier=https://fhir.experiments.com/PatientAlias/BACKENDID/HCA:KYA|J516754"));
        //encounter.addLocation().setLocation(new Reference("Location?identifier=https://fhir.experiments.com/LocationType/FACILITY|COCQA1A"));
        //encounter.addLocation().setLocation(new Reference("Location?identifier=https://fhir.experiments.com/LocationType/UNIT|J.SOLIS"));
        //encounter.addParticipant().setIndividual(new Reference("Practitioner?identifier=https://fhir.experiments.com/PersonAlias/BACKENDID/HCA:KYA|PROVIDER.SIALBJA"));

        // Create a bundle that will be used as a transaction
        Bundle bundle = new Bundle();
        bundle.setType(Bundle.BundleType.TRANSACTION);

        bundle.addEntry()
                .setFullUrl(encounter.getId())
                .setResource(encounter)
                .getRequest()
                .setUrl("Encounter?identifier=https://fhir.experiments.com/EncounterAlias/BACKENDID/HCA:KYA:COCQA1A|J000440201:J516754:20190826.0932:REF:J00021227198")
                //.setIfNoneExist("identifier=https://fhir.experiments.com/EncounterAlias/BACKENDID/HCA:KYA:COCQA1A|J000440201:J516754:20190826.0932:REF:J00021227198")
                .setMethod(Bundle.HTTPVerb.PUT);

        bundle.addEntry()
                .setFullUrl(patient.getId())
                .setResource(patient)
                .getRequest()
                .setUrl("Patient?identifier=https://fhir.experiments.com/PatientAlias/BACKENDID/HCA:KYA|J516754")
                //.setIfNoneExist("identifier=https://fhir.experiments.com/PatientAlias/BACKENDID/HCA:KYA|J516754")
                .setMethod(Bundle.HTTPVerb.PUT);

        bundle.addEntry()
                .setFullUrl(facility.getId())
                .setResource(facility)
                .getRequest()
                .setUrl("Location?identifier=https://fhir.experiments.com/LocationType/FACILITY|COCQA1A")
                //.setIfNoneExist("identifier=https://fhir.experiments.com/LocationType/FACILITY|COCQA1A")
                .setMethod(Bundle.HTTPVerb.PUT);

        bundle.addEntry()
                .setFullUrl(unit.getId())
                .setResource(unit)
                .getRequest()
                .setUrl("Location?identifier=https://fhir.experiments.com/LocationType/UNIT|J.SOLIS")
                //.setIfNoneExist("identifier=https://fhir.experiments.com/LocationType/UNIT|J.SOLIS")
                .setMethod(Bundle.HTTPVerb.PUT);

        bundle.addEntry()
                .setFullUrl(attending.getId())
                .setResource(attending)
                .getRequest()
                .setUrl("Practitioner?identifier=https://fhir.experiments.com/PersonAlias/BACKENDID/HCA:KYA|PROVIDER.SIALBJA")
                //.setIfNoneExist("identifier=https://fhir.experiments.com/PersonAlias/BACKENDID/HCA:KYA|PROVIDER.SIALBJA")
                .setMethod(Bundle.HTTPVerb.PUT);

        // Log the request
        log.info(ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(bundle));

        Bundle response = client.transaction().withBundle(bundle).execute();

        // Log the response
        log.info(ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(response));
    }

    public static void main(String[] args) {
        ConditionalUpdateExample conditionalUpdateExample = new ConditionalUpdateExample();
        conditionalUpdateExample.run();
    }
}