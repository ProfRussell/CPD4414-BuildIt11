/*
 * Copyright 2015 Len Payne <len.payne@lambtoncollege.ca>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package services;

import entities.Product;
import java.util.List;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.UserTransaction;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 *
 * @author Len Payne <len.payne@lambtoncollege.ca>
 */
@Path("/product")
@RequestScoped
public class ProductREST {

    @PersistenceContext(unitName = "CPD4414-BuildIt11PU")
    EntityManager em;

    @Inject
    UserTransaction transaction;

    List<Product> productList;

    @GET
    @Produces("application/json")
    public Response getAll() {
        JsonArrayBuilder json = Json.createArrayBuilder();
        Query q = em.createNamedQuery("Product.findAll");
        productList = q.getResultList();
        for (Product p : productList) {
            json.add(p.toJSON());
        }
        return Response.ok(json.build().toString()).build();
    }

    @GET
    @Path("{id}")
    @Produces("application/json")
    public Response getById(@PathParam("id") int id) {
        Query q = em.createQuery("SELECT p FROM Product p WHERE p.productId = :productId");
        q.setParameter("productId", id);
        Product p = (Product) q.getSingleResult();
        return Response.ok(p.toJSON().toString()).build();
    }

    @POST
    @Consumes("application/json")
    public Response add(JsonObject json) {
        Response result;
        try {
            transaction.begin();
            Product p = new Product(json);
            em.persist(p);
            transaction.commit();
            result = Response.ok().build();
        } catch (Exception ex) {
            result = Response.status(500).entity(ex.getMessage()).build();
        }
        return result;
    }
    
    @PUT
    @Consumes("application/json")
    public Response edit(JsonObject json) {
        Response result;
        try {
            transaction.begin();
            Product p = (Product) em.createNamedQuery("Product.findByProductId")
                    .setParameter("productId", json.getInt("productId")).getSingleResult();
            p.setName(json.getString("name"));
            p.setDescription(json.getString("description"));
            p.setQuantity(json.getInt("quantity"));
            em.persist(p);
            transaction.commit();
            result = Response.ok().build();
        } catch (Exception ex) {
            result = Response.status(500).entity(ex.getMessage()).build();
        }
        return result;
    }

    @DELETE
    @Path("{id}")
    public Response delete(@PathParam("id") int id) {
        Response result;
        try {
            transaction.begin();
            Product p = (Product) em.createNamedQuery("Product.findByProductId")
                    .setParameter("productId", id).getSingleResult();
            em.remove(p);
            transaction.commit();
            result = Response.ok().build();
        } catch (Exception ex) {
            result = Response.status(500).entity(ex.getMessage()).build();
        }
        return result;
    }
}
