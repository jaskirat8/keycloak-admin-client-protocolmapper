package com.jaskirat.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.ProtocolMapperRepresentation;
import org.keycloak.models.utils.ModelToRepresentation;
import org.keycloak.protocol.ProtocolMapperUtils;
import org.keycloak.protocol.oidc.mappers.ScriptBasedOIDCProtocolMapper;


/**
 * Hello world!
 */
public final class App {
    private App() {
    }

    static String id = null;
    /**
     * Says hello to the world.
     * @param args The arguments of the program.
     */
    public static void main(String[] args) {

        //ONly for Log4j not required in actual project
        ConsoleAppender console = new ConsoleAppender(); //create appender
        String PATTERN = "%d [%p|%c|%C{1}] %m%n";
        console.setLayout(new PatternLayout(PATTERN)); 
        console.setThreshold(Level.FATAL);
        console.activateOptions();
        Logger.getRootLogger().addAppender(console);


        //Main code starts from here
        try {
            String clientId = "idptest";
			Keycloak kc = Keycloak.getInstance("<Keycloak Server Url>", "master",
                    "admin", "admin", "admin-cli");
            kc.realm("master").clients().findByClientId(clientId).forEach((a) -> id = a.getId());
            ClientRepresentation client = new ClientRepresentation();
			client.setRedirectUris(Arrays.asList("*"));
			client.setClientId(clientId);
			client.setDirectAccessGrantsEnabled(true);
			client.setImplicitFlowEnabled(true);
			client.setPublicClient(true);
            client.setWebOrigins(Arrays.asList("*"));
            List<ProtocolMapperRepresentation> list = new ArrayList<ProtocolMapperRepresentation>();
            list.add(createScriptMapper("idp_email", "", "idp_email", "String", true, true, "var email = user.getEmail();\nvar idp_email = email.substring(0 , email.indexOf('@'));\nexports = idp_email;", false));
            client.setProtocolMappers(list);
            if(id == null){
                kc.realm("master").clients().create(client);    
            }
            else{
                kc.realm("master").clients().get(id).remove();
                kc.realm("master").clients().create(client);
            }
		} catch (Exception e) {
            System.out.println(e.getMessage());
		}
    }

    public static ProtocolMapperRepresentation createScriptMapper(String name,
                                                                  String userAttribute,
                                                                  String tokenClaimName,
                                                                  String claimType,
                                                                  boolean accessToken,
                                                                  boolean idToken,
                                                                  String script,
                                                                  boolean multiValued) {

        return ModelToRepresentation.toRepresentation(
          ScriptBasedOIDCProtocolMapper.create(name, userAttribute, tokenClaimName, claimType, accessToken, idToken, script, multiValued)
        );
    }
}
