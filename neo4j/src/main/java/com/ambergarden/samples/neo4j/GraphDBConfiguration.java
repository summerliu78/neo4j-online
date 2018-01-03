package com.ambergarden.samples.neo4j;

import org.neo4j.ogm.session.SessionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.config.Neo4jConfiguration;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

/**
 * Spring JavaConfig configuration class to setup a Spring container and infrastructure components.
 */
@Configuration
@EnableNeo4jRepositories(basePackages = "com.ambergarden.samples.neo4j.repositories")
//@EnableTransactionManagement
public class GraphDBConfiguration extends Neo4jConfiguration {

   @Bean
   public org.neo4j.ogm.config.Configuration getConfiguration() {
      org.neo4j.ogm.config.Configuration config = new org.neo4j.ogm.config.Configuration();
      try {
         Properties pps = new Properties();
         InputStream inStr = GraphDBConfiguration.class.getClassLoader().getResourceAsStream("qcellcore.properties");
         pps.load(inStr);
         String url = pps.getProperty("url");
         String name = pps.getProperty("name");
         String pass = pps.getProperty("pass");
         // TODO: Temporary uses the embedded driver. We need to switch to http
         config.driverConfiguration()
//            .setDriverClassName("org.neo4j.ogm.drivers.embedded.driver.EmbeddedDriver")
                 //            .setURI("file:/D:/temp/data/");
                 .setDriverClassName("org.neo4j.ogm.drivers.http.driver.HttpDriver")
                 .setURI(url)
                 .setCredentials(name, pass);

      } catch (Exception e) {
         e.printStackTrace();
      }
      return config;
   }

   @Override
   @Bean
   public SessionFactory getSessionFactory() {
      // Return the session factory which also includes the persistent entities
      return new SessionFactory(getConfiguration(), "com.ambergarden.samples.neo4j.entities");
   }
}