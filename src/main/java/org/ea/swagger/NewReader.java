package org.ea.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.parser.OpenAPIV3Parser;

public class NewReader {
    private static OpenAPI openAPI;

    public static void main(String[] args) {
        openAPI = new OpenAPIV3Parser().read("./swagger.json");
        System.out.println(openAPI.getInfo());
        System.out.println("Description: " + openAPI.getInfo().getDescription());
        openAPI.getPaths().forEach((key, item) -> {
            System.out.println(key);
            printOperations(item);
        });
    }

    private static void printOperations(PathItem item) {
        if(item.getHead() != null) {
            System.out.print("HEAD - ");
            printOperation(item.getHead());
        }
        if(item.getGet() != null) {
            System.out.print("GET - ");
            printOperation(item.getGet());
        }
        if(item.getPost() != null) {
            System.out.print("POST - ");
            printOperation(item.getPost());
        }
        if(item.getPatch() != null) {
            System.out.print("PATCH - ");
            printOperation(item.getPatch());
        }
        if(item.getPut() != null) {
            System.out.print("PUT - ");
            printOperation(item.getPut());
        }
        if(item.getDelete() != null) {
            System.out.print("DELETE - ");
            printOperation(item.getDelete());
        }

    }

    private static void printOperation(Operation op) {
        System.out.println(op.getOperationId());
        System.out.println("Parameters:");
        if(op.getParameters() != null) {
            for (Parameter p : op.getParameters()) {
                System.out.println(p.getName() + " : " + p.getSchema().getType());
            }
            if(op.getRequestBody() != null) {
                printBody(op.getRequestBody());
            }
        }
        System.out.println();
        printResponses(op.getResponses());
        System.out.println();
    }

    private static void printBody(RequestBody requestBody) {
        System.out.print("BODY: ");
        requestBody.getContent().forEach((key, item) -> {
            System.out.println(key);
            printReference(item.getSchema());
        });
    }

    private static void printResponses(ApiResponses responses) {
        System.out.println("Responses:");
        responses.forEach((key, item) -> {
            System.out.println(key + ": " + item.getDescription());
            item.getContent().forEach((name, media) -> {
                System.out.println(name);
                printReference(media.getSchema());
            });
        });
    }

    private static void printReference(Schema schema) {
        if(schema instanceof ArraySchema) {
            ArraySchema as = (ArraySchema) schema;
            printReference(as.getItems());
        }

        String componentName = getComponentName(schema.get$ref());
        if(componentName != null) {
            Object objSchema = openAPI.getComponents().getSchemas().get(componentName);

            Schema componentSchema;
            if(objSchema instanceof ArraySchema) {
                ArraySchema as = (ArraySchema) objSchema;
                printReference(as.getItems());
                return;
            } else {
                componentSchema = (Schema) objSchema;
            }

            componentSchema.getProperties().forEach((key, item) -> {
                if(item instanceof Schema) {
                    System.out.println("  " + key + " : " + ((Schema)item).getType());
                } else {
                    System.out.println("  " + key + " : " + item.getClass().getSimpleName());
                }
            });
        }
    }

    private static String getComponentName(String s) {
        if(s == null) return null;
        if(s.startsWith("#/components/schemas/")) {
            return s.substring("#/components/schemas/".length());
        }
        return null;
    }
}