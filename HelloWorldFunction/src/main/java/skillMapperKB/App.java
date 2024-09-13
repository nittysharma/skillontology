package skillMapperKB;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import software.amazon.awssdk.services.bedrockagentruntime.BedrockAgentRuntimeClient;
import software.amazon.awssdk.services.bedrockagentruntime.model.*;

/**
 * Handler for requests to Lambda function.
 */
public class App implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    static String  modelId="";
    static String promptText="";
    static String knowledgeBaseId="";

    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input, final Context context) {
        modelId=System.getenv("modelId");
        promptText=System.getenv("promptText");
        knowledgeBaseId=System.getenv("knowledgeBaseId");
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("X-Custom-Header", "application/json");

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent()
                .withHeaders(headers);
            String output = retrieveAndGenerateRefundWindowKB(input.getQueryStringParameters().get("extractedSkill")).output().text();
            return response
                    .withStatusCode(200)
                    .withBody(output);
    }
    public static RetrieveAndGenerateResponse retrieveAndGenerateRefundWindowS3(String extratedSkill) {
        S3ObjectDoc s3ObjectDoc=S3ObjectDoc.builder().uri("s3://skillmapperbucket/skills.pdf").build();
        ExternalSource externalSource= ExternalSource.builder().sourceType(ExternalSourceType.S3).s3Location(s3ObjectDoc).build();
        ExternalSourcesRetrieveAndGenerateConfiguration externalSourcesRetrieveAndGenerateConfiguration =ExternalSourcesRetrieveAndGenerateConfiguration
                .builder().sources(externalSource)
                .modelArn(modelId).build();
        RetrieveAndGenerateConfiguration retrieveAndGenerateConfiguration=RetrieveAndGenerateConfiguration.builder().
                type(RetrieveAndGenerateType.EXTERNAL_SOURCES).externalSourcesConfiguration(externalSourcesRetrieveAndGenerateConfiguration).build();
        RetrieveAndGenerateInput input= RetrieveAndGenerateInput.builder().
                text(promptText+
                        "<text>" +extratedSkill+
                        "</text>").build();
        RetrieveAndGenerateRequest retrieveAndGenerateRequest= RetrieveAndGenerateRequest.builder().input(input).
                retrieveAndGenerateConfiguration(retrieveAndGenerateConfiguration).build();
        RetrieveAndGenerateResponse retrieveAndGenerateResponse= BedrockAgentRuntimeClient.builder().build().
                retrieveAndGenerate(retrieveAndGenerateRequest);
        return retrieveAndGenerateResponse;
    }


    public static RetrieveAndGenerateResponse retrieveAndGenerateRefundWindowKB(String extratedSkill) {
        KnowledgeBaseRetrieveAndGenerateConfiguration knowledgeBaseRetrieveAndGenerateConfiguration =KnowledgeBaseRetrieveAndGenerateConfiguration
                .builder().knowledgeBaseId(knowledgeBaseId).modelArn(modelId).build();
        RetrieveAndGenerateConfiguration retrieveAndGenerateConfiguration=RetrieveAndGenerateConfiguration.builder().
                type(RetrieveAndGenerateType.KNOWLEDGE_BASE).knowledgeBaseConfiguration(knowledgeBaseRetrieveAndGenerateConfiguration).build();
        RetrieveAndGenerateInput input= RetrieveAndGenerateInput.builder().
                text(promptText +
                        "<text>" +extratedSkill+
                        "</text>").build();
        RetrieveAndGenerateRequest retrieveAndGenerateRequest= RetrieveAndGenerateRequest.builder().input(input).
                retrieveAndGenerateConfiguration(retrieveAndGenerateConfiguration).build();
        RetrieveAndGenerateResponse retrieveAndGenerateResponse= BedrockAgentRuntimeClient.builder().build().
                retrieveAndGenerate(retrieveAndGenerateRequest);
        return retrieveAndGenerateResponse;
    }
}
