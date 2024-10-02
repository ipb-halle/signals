package de.ipb_halle.signals.entity;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.ipb_halle.signals.rest.*;
import jakarta.inject.Inject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

public class SignalsEntitiesTestCall {

    @Inject
    SignalsEntityDbService signalsEntityDbService;

    @Inject
    RestClient restClient;

    @Inject
    RestClientImpl restClientImpl;

    public final String SIGNALS_ENTITY_ENDPOINT = "/entities";
    public final String PARAMETER_INCLUDE_TYPES = "includeTypes";


    public List<SignalsEntity> receiveTheEntitiesFromSignals() {
        List<SignalsEntity> entities = new ArrayList<>();

        try {
            restClientImpl.setMethod(Method.GET).setEndpoint("/entities").putUriParameter(PARAMETER_INCLUDE_TYPES, "journal");

            restClientImpl.toString();

            System.out.println("trying to get response:.......");
            String jsonResponse = "no response";
            try {
                //answer as String, which should be parsed
                jsonResponse = restClientImpl.execute().getResponse();

            } catch (RuntimeException e) {
                throw new RuntimeException("ERROR RECEIVED!");
            } finally {
                System.out.println("got response!!!");
                //  System.out.println("THIS IS JSON FROM RESPONSE: " + jsonResponse);

            }

            // parsing a string to JSONObject Java
            JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();

            //creating JSONDataArray with all entities e.g. from 0 to 20
            JsonArray dataArray = jsonObject.getAsJsonArray("data");
            //System.out.println(Arrays.toString(new JsonArray[]{dataArray}));

            //handling each entity separately
            for (JsonElement element : dataArray) {
                //debugging:
                //System.out.println("ELEMENET : " + element);

                JsonObject entityObject = element.getAsJsonObject();
                SignalsEntity entity = parseJsonToSignalsEntity(entityObject);

                //debugging
                //System.out.println("ENTITY ID : " + entity.getId());

                entities.add(entity);
                // signalsEntityDbService.save(entity);
            }

        } catch (IOException | URISyntaxException | UnexpectedResponseCodeException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return entities;
    }

    private SignalsEntity parseJsonToSignalsEntity(JsonObject jsonObject) {
        SignalsEntity entity = new SignalsEntity();

        //information about available entities
        System.out.printf("Entry Set %s", jsonObject.entrySet().toArray()[3] + "\n");

        //taking only attributes from array data
        JsonObject attributes = jsonObject.getAsJsonObject("attributes");
        //display attributes info
        System.out.println("THESE ARE ATTRIBUTES : " + attributes.entrySet());

        try {
            // test if it is working correct
            System.out.println("NAME : " + attributes.get("type").toString());

            entity.setId(jsonObject.get("id").getAsString());
            entity.setType(jsonObject.get("type").getAsString());
            entity.setName(attributes.has("name") ? attributes.get("name").getAsString() : null);
            entity.setDescription(attributes.has("description") ? attributes.get("description").getAsString() : null);
            entity.setCreatedAt(attributes.has("createdAt") ? attributes.get("createdAt").getAsString() : null);
            entity.setEditedAt(attributes.has("editedAt") ? attributes.get("editedAt").getAsString() : null);
            entity.setDigest(attributes.has("digest") ? attributes.get("digest").getAsString() : null);
        } catch (RuntimeException e) {
            throw new RuntimeException("attributes are zero!" + e);
        } finally {

            System.out.println("attributes are not zero!");
        }

        /
        return entity;
    }


}
