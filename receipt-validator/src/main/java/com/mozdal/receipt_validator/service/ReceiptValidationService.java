package com.mozdal.receipt_validator.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class ReceiptValidationService {

    // sandbox URL
    private final static String SANDBOX_URL = "https://sandbox.itunes.apple.com/verifyReceipt";
    // production URL
    private final static String PRODUCTION_URL = "https://buy.itunes.apple.com/verifyReceipt";
    //Subscription Secret
    private final static String SUBSCRIPTION_SECRET = "yourSecretHere";
    //Bundle Id
    private final static String BUNDLE_ID = "yourBundleIdentifierHere";
    //Application Version
    private final static String APPLICATION_VERSION = "1";


    public HashMap<String, Object> validateReceipt(final String receipt) throws Exception {

        try
        {
            JsonParser parser = new JsonParser();

            JsonObject receiptObj = parser.parse(receipt).getAsJsonObject();
            receiptObj.addProperty("password", SUBSCRIPTION_SECRET);
            String receiptWithSecret = receiptObj.toString();

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(receiptWithSecret, headers);
            String jsonAnswer = restTemplate.postForObject(SANDBOX_URL, entity, String.class);

            JsonObject root = new JsonParser().parse(jsonAnswer).getAsJsonObject();
            Integer status = root.get("status").getAsInt();
            if(status != 0) {
                return mapStatus(status);
            }
            return checkValidation(root);
        }
        catch (Exception e)
        {
            throw new Exception(e.getMessage());
        }
    }

    private HashMap<String, Object> checkValidation(JsonObject root) {
        HashMap<String, Object> resultMap = new HashMap<>();
        //To be determined with which field to compare
        String productId;
        String bundleId = root.get("receipt").getAsJsonObject().get("bundle_id").getAsString();
        String applicationVersion = root.get("receipt").getAsJsonObject().get("application_version").getAsString();
        JsonArray latestReceiptInfoArray = root.get("latest_receipt_info").getAsJsonArray();

        if(applicationVersion.equals(APPLICATION_VERSION) && bundleId.equals(BUNDLE_ID)) {
            if(latestReceiptInfoArray.size() > 0) {
                JsonObject latestReceipt = latestReceiptInfoArray.get(latestReceiptInfoArray.size() - 1).getAsJsonObject();
                productId = latestReceipt.get("product_id").getAsString();
                Long expiresDateMs = latestReceipt.get("expires_date_ms").getAsLong();
                if(expiresDateMs > System.currentTimeMillis()) {
                    //Subscription is valid
                    resultMap.put("result", true);
                    resultMap.put("message", "Subscription is valid");
                    resultMap.put("expiration_date", expiresDateMs);
                } else {
                    //Subscription expired
                    resultMap.put("result", false);
                    resultMap.put("message", "Subscription expired");
                }
                return resultMap;
            } else {
                //No items in latest_receipt, considered valid
                resultMap.put("result", false);
                resultMap.put("message", "Subscription expired");
                return  resultMap;
            }
        } else {
            resultMap.put("result", false);
            resultMap.put("message", "Inconsistency in receipt information");
            return resultMap;
        }
    }

    private HashMap<String, Object> mapStatus(int status) {
        HashMap<String,Object> resultMap = new HashMap<>();
        switch (status) {
            case 0:
                resultMap.put("result", true);
                resultMap.put("message", "success");
                return resultMap;
            case 21000: resultMap.put("message", "App store could not read"); break;
            case 21002: resultMap.put("message", "Data was malformed"); break;
            case 21003: resultMap.put("message", "Receipt not authenticated"); break;
            case 21004: resultMap.put("message", "Shared secret does not match"); break;
            case 21005: resultMap.put("message", "Receipt server unavailable"); break;
            case 21006: resultMap.put("message", "Receipt valid but sub expired"); break;
            case 21007: resultMap.put("message", "Sandbox receipt sent to Production environment"); break;
            case 21008: resultMap.put("message", "Production receipt sent to Sandbox environment"); break;
            default:
                resultMap.put("message", "Unknown error: status code");
        }
        resultMap.put("result", false);
        return resultMap;
    }
}
