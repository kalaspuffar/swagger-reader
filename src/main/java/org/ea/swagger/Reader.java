package org.ea.swagger;

import io.swagger.models.*;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.refs.RefFormat;
import io.swagger.parser.SwaggerParser;

import java.util.Map;

public class Reader {
    public static void main(String[] args) {
        Swagger swagger = new SwaggerParser().read("swagger.json");

        System.out.println(swagger.getInfo().getDescription());

        for(Map.Entry<String, Path> entry : swagger.getPaths().entrySet()) {
            System.out.println(entry.getKey());

            printOperations(swagger, entry.getValue().getOperationMap());
        }
    }

    private static void printOperations(Swagger swagger, Map<HttpMethod, Operation> operationMap) {
        for(Map.Entry<HttpMethod, Operation> op : operationMap.entrySet()) {
            System.out.println(op.getKey() + " - " + op.getValue().getOperationId());
            System.out.print("Parameters: ");
            for(Parameter p : op.getValue().getParameters()) {
                System.out.print(p.getName() + ", ");
            }
            System.out.println();
            printResponses(swagger, op.getValue().getResponses());
            System.out.println();
        }
    }

    private static void printResponses(Swagger swagger, Map<String, Response> responseMap) {
        System.out.println("Responses:");
        for(Map.Entry<String, Response> response : responseMap.entrySet()) {
            System.out.println(response.getKey() + ": " + response.getValue().getDescription());

            if(response.getValue().getSchema() instanceof ArrayProperty) {
                ArrayProperty ap = (ArrayProperty)response.getValue().getSchema();
                if(ap.getItems() instanceof RefProperty) {
                    RefProperty rp = (RefProperty)ap.getItems();
                    printReference(swagger, rp);
                }
            }
        }
    }

    private static void printReference(Swagger swagger, RefProperty rp) {
        if(rp.getRefFormat().equals(RefFormat.INTERNAL) &&
                swagger.getDefinitions().containsKey(rp.getSimpleRef())) {
            Model m = swagger.getDefinitions().get(rp.getSimpleRef());
            for(Map.Entry<String, Property> propertyEntry : m.getProperties().entrySet()) {
                System.out.println("  " + propertyEntry.getKey() + " : " + propertyEntry.getValue().getType());
            }
        }
    }
}
