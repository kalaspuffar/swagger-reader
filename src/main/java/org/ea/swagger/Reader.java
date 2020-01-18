package org.ea.swagger;

import io.swagger.models.*;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.PathParameter;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.refs.RefFormat;
import io.swagger.parser.SwaggerParser;

import java.util.Map;

public class Reader {
    public static void main(String[] args) {
        Swagger swagger = new SwaggerParser().read("swagger.json");

        System.out.println("Description: " + swagger.getInfo().getDescription());

        for(Map.Entry<String, Path> entry : swagger.getPaths().entrySet()) {
            System.out.println(entry.getKey());

            printOperations(swagger, entry.getValue().getOperationMap());
        }
    }

    private static void printOperations(Swagger swagger, Map<HttpMethod, Operation> operationMap) {
        for(Map.Entry<HttpMethod, Operation> op : operationMap.entrySet()) {
            System.out.println(op.getKey() + " - " + op.getValue().getOperationId());
            System.out.println("Parameters:");
            for(Parameter p : op.getValue().getParameters()) {
                if(p instanceof BodyParameter) {
                    printBody(swagger, (BodyParameter) p);
                } else {
                    String paramType = p.getClass().getSimpleName();
                    if(p instanceof PathParameter) {
                        paramType = "path";
                    }
                    System.out.println(p.getName() + " : " + paramType);
                }
            }
            System.out.println();
            printResponses(swagger, op.getValue().getResponses());
            System.out.println();
        }
    }

    private static void printBody(Swagger swagger, BodyParameter p) {
        System.out.println("BODY: ");

        RefProperty rp = new RefProperty(p.getSchema().getReference());
        printReference(swagger, rp);
    }

    private static void printResponses(Swagger swagger, Map<String, Response> responseMap) {
        System.out.println("Responses:");
        for(Map.Entry<String, Response> response : responseMap.entrySet()) {
            System.out.println(response.getKey() + ": " + response.getValue().getDescription());

            if(response.getValue().getSchema() instanceof RefProperty) {
                RefProperty rp = (RefProperty)response.getValue().getSchema();
                printReference(swagger, rp);
            }

            if(response.getValue().getSchema() instanceof ArrayProperty) {
                ArrayProperty ap = (ArrayProperty)response.getValue().getSchema();
                if(ap.getItems() instanceof RefProperty) {
                    RefProperty rp = (RefProperty)ap.getItems();
                    System.out.println(rp.getSimpleRef() + "[]");
                    printReference(swagger, rp);
                }
            }
        }
    }

    private static void printReference(Swagger swagger, RefProperty rp) {
        if(rp.getRefFormat().equals(RefFormat.INTERNAL) &&
                swagger.getDefinitions().containsKey(rp.getSimpleRef())) {
            Model m = swagger.getDefinitions().get(rp.getSimpleRef());

            if(m instanceof ArrayModel) {
                ArrayModel arrayModel = (ArrayModel)m;
                System.out.println(rp.getSimpleRef() + "[]");
                if(arrayModel.getItems() instanceof RefProperty) {
                    RefProperty arrayModelRefProp = (RefProperty)arrayModel.getItems();
                    printReference(swagger, arrayModelRefProp);
                }
            }

            if(m.getProperties() != null) {
                for (Map.Entry<String, Property> propertyEntry : m.getProperties().entrySet()) {
                    System.out.println("  " + propertyEntry.getKey() + " : " + propertyEntry.getValue().getType());
                }
            }
        }
    }
}
