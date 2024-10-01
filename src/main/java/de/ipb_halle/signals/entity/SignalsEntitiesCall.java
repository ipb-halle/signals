package de.ipb_halle.signals.entity;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.ipb_halle.signals.rest.Method;
import de.ipb_halle.signals.rest.RestClient;
import de.ipb_halle.signals.rest.UnexpectedResponseCodeException;
import jakarta.inject.Inject;
import org.hibernate.annotations.Source;

import java.io.IOException;
import java.net.URISyntaxException;

public class SignalsEntitiesCall {

    @Inject
    SignalsEntityDbService signalsEntityDbService;

    @Inject
    RestClient restClient;

    public void receiveTheEntitiesFromSignals() {
        SignalsEntity entity = null;

        try {
            restClient.setEndpoint("/entities");
            restClient.setMethod(Method.GET);
            String jsonResponse = restClient.execute().getResponse();

            JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
            entity = parseJsonToSignalsEntity(jsonObject);

            System.out.println(jsonObject);

            System.out.println(entity.getId());

            signalsEntityDbService.save(entity);

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        } catch (UnexpectedResponseCodeException e) {
            throw new RuntimeException(e);
        }
    }

    private SignalsEntity parseJsonToSignalsEntity(JsonObject jsonObject){
        SignalsEntity entity = new SignalsEntity();
        entity.setId(jsonObject.get("id").getAsString());
        entity.setType(jsonObject.get("type").getAsString());
        return entity;
    }

}
