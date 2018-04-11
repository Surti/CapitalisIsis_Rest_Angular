/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.mavenproject1;

import com.google.gson.Gson;
import generated.PallierType;
import generated.ProductType;
import generated.World;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PUT;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;
/**
 * REST Web Service
 *
 * @author leman
 */
@Path("generic")
public class GenericResource {

    @Context
    private UriInfo context;
    private Services s;

    /**
     * Creates a new instance of GenericResource
     */
    public GenericResource() {
         s = new Services();
    }

    /**
     * Retrieves representation of an instance of com.mycompany.mavenproject1.GenericResource
     * @return an instance of java.lang.String
     */
    @GET
    @Path("world")
    @Produces("application/xml")
    public Response getXml(@Context HttpServletRequest request) {
        String username = request.getHeader("X-User");
        try {
                  
            World w = s.getWorld(username);
            
            return Response.ok(w).build();
        } catch (JAXBException ex) {
            Logger.getLogger(GenericResource.class.getName()).log(Level.SEVERE, null, ex);
        }
            
            return Response.status(Response.Status.NOT_FOUND).build();
    }
    
    @GET
    @Path("world-json")
    @Produces("application/json")
    public Response getXmlJson(@Context HttpServletRequest request) {
        String username = request.getHeader("X-User");
        try {
            System.out.println(username);
            World w = s.getWorld(username);
                      
            return Response.ok(new Gson().toJson(w)).build();
         
        } catch (JAXBException ex) {
            Logger.getLogger(GenericResource.class.getName()).log(Level.SEVERE, null, ex);
        }
            
            return Response.status(Response.Status.NOT_FOUND).build();
    }
    
        @PUT
    @Path("product")
    @Consumes("application/json")
    public void putProduct(String data, @Context HttpServletRequest request) throws JAXBException {
        String username =  request.getHeader("X-user");
        ProductType product = new Gson().fromJson(data, ProductType.class);
        s.updateProduct(username, product);
        System.out.println("PUT PRODUCT "+ data);
    }
     
    @PUT
    @Path("manager")
    @Consumes("application/json")
    public void putManager(String data, @Context HttpServletRequest request) throws JAXBException {
        String username =  request.getHeader("X-user");
        PallierType manager = new Gson().fromJson(data, PallierType.class);
        s.updateManager(manager, username);
        System.out.println("PUT MANAGER "+ data);
    }

    /**
     * PUT method for updating or creating an instance of GenericResource
     * @param content representation for the resource
     */
    @PUT
    @Path("world")
    @Consumes(MediaType.APPLICATION_XML)
    public void putXml(String content) {
    }
}
