/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://jersey.dev.java.net/CDDL+GPL.html
 * or jersey/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at jersey/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.jersey.impl.json.xml.ns;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.container.filter.LoggingFilter;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.api.json.JSONJAXBContext;
import com.sun.jersey.impl.AbstractResourceTester;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

/**
 *
 * @author Srinivas.Bhimisetty@Sun.COM
 */
public class JSONXMLNamespaceTester  extends AbstractResourceTester {

    public JSONXMLNamespaceTester(String testName) {
        super(testName);
    }

    @Provider
    public static class JAXBContextResolver implements ContextResolver<JAXBContext> {
        private JAXBContext context;
        private final Class[] cTypes = {
                                        ContactType.class,
                                        CountryType.class,
                                        CustomerOrderType.class,
                                        CustomerType.class,
                                        DeliveryAddressType.class,
                                        DeliveryType.class,
                                        ItemType.class,
                                        LineItemType.class,
                                        OrderItemType.class,
                                        OrderLineType.class,
                                        OrderType.class,
                                        OriginatorCustomerPartyType.class,
                                        PartyNameType.class,
                                        PartyType.class,
                                        PostalAddressType.class,
                                        PriceType.class
                                       };
        private final Set<Class> types;
        public JAXBContextResolver() {
            Map<String, String> ns2json = new HashMap<String, String>();
            ns2json.put("urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2","cac2");
            ns2json.put("urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2","cbc2");
            ns2json.put("urn:oasis:names:specification:ubl:schema:xsd:Order-2","o2");
            try {
                this.types = new HashSet<Class>(Arrays.asList(cTypes));
                this.context = new JSONJAXBContext(JSONConfiguration.mapped().xml2JsonNs(ns2json).rootUnwrapping(false).build(), cTypes);
            } catch (JAXBException ex) {
                throw new RuntimeException(ex);
            }
        }

        public JAXBContext getContext(Class<?> c) {
            return types.contains(c) ? context : null;
        }
    }

    @Path("/")
    public static class OrderResource {
        @POST
        @Consumes("application/json")
        @Produces({"application/json"})
        public OrderType bookOrder(CustomerOrderType customerOrder) {
            // get customer details
            CustomerType customer = customerOrder.getCustomer();

            OrderType orderType = new OrderType();

            // set customer details in order
            CountryType country = new CountryType();
            country.setIdentificationCode(customer.getCountryCode());

            PostalAddressType postalAddress = new PostalAddressType();
            postalAddress.setBuildingNumber(customer.getBuildingNumber());
            postalAddress.setStreetName(customer.getStreetName());
            postalAddress.setCityName(customer.getCityName());
            postalAddress.setPostalZone(customer.getPinCode());
            postalAddress.setCountry(country);

            ContactType contact = new ContactType();
            contact.setName(customer.getName());
            contact.setTelephone("9901999388");

            PartyNameType partyName = new PartyNameType();
            partyName.setName(customer.getName());

            PartyType party = new PartyType();
            party.setPartyName(partyName);
            party.setPostalAddress(postalAddress);
            party.setContact(contact);

            OriginatorCustomerPartyType originatorCustomerParty = new OriginatorCustomerPartyType();
            originatorCustomerParty.setPartyType(party);

            // set delivery address
            DeliveryAddressType deliveryAddress = new DeliveryAddressType();
            deliveryAddress.setBuildingNumber(customer.getBuildingNumber());
            deliveryAddress.setStreetName(customer.getStreetName());
            deliveryAddress.setCityName(customer.getCityName());
            deliveryAddress.setPostalZone(customer.getPinCode());
            deliveryAddress.setCountry(country);

            DeliveryType delivery = new DeliveryType();
            delivery.setDeliveryAddressType(deliveryAddress);

            // set order item
            OrderItemType orderItem = new OrderItemType();
            orderItem.setName("DummyItem1");
            orderItem.setDescription("Dummy Description for dummyItem1....");

            PriceType price = new PriceType();
            price.setBaseQuantity(100);
            price.setPriceAmount(10.00);
            price.setOrderItemType(orderItem);

            LineItemType lineItem = new LineItemType();
            lineItem.setId("1");
            lineItem.setPriceType(price);

            OrderLineType orderLine = new OrderLineType();
            orderLine.setLineItem(lineItem);

            // set order data
            orderType.setDelivery(delivery);
            orderType.setId("1");
            orderType.setOrderLine(orderLine);
           orderType.setOriginatorCustomerParty(originatorCustomerParty);

            return orderType;
        }

        @Path("test")
        @GET
        @Produces("application/json")
        public CustomerOrderType getCustomerOrder() {
            CustomerType customer = new CustomerType();
            customer.setBuildingNumber("10A");
            customer.setCityName("Bangalore");
            customer.setCountryCode("IND");
            customer.setName("Naresh");
            customer.setPinCode("560026");
            customer.setStreetName("Richmond Town");

            ItemType item = new ItemType();
            item.setItemCode("Item01");
            item.setQuantity(100);


            CustomerOrderType customerOrder = new CustomerOrderType();
            customerOrder.setCustomer(customer);
            customerOrder.setItem(item);
            return customerOrder;
        }
    }

    public void testCustomerOrder() throws Exception {
        JAXBContextResolver cr = new JAXBContextResolver();
        ResourceConfig rc = new DefaultResourceConfig(OrderResource.class);
        rc.getSingletons().add(cr);
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS, LoggingFilter.class.getName());
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS, LoggingFilter.class.getName());
        initiateWebApplication(rc);

        ClientConfig cc = new DefaultClientConfig();
        cc.getClasses().add(cr.getClass());
        WebResource r = resource("/", cc);
        r.path("test").accept("application/json").get(ClientResponse.class);        
    }

    public void testOrderResource() throws Exception {
        JAXBContextResolver cr = new JAXBContextResolver();
        ResourceConfig rc = new DefaultResourceConfig(OrderResource.class);
        rc.getSingletons().add(cr);
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS, LoggingFilter.class.getName());
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS, LoggingFilter.class.getName());
        initiateWebApplication(rc);

        ClientConfig cc = new DefaultClientConfig();
        cc.getClasses().add(cr.getClass());
        WebResource r = resource("/", cc);
        
        CustomerType customer = new CustomerType();
        customer.setBuildingNumber("10A");
        customer.setCityName("Bangalore");
        customer.setCountryCode("IND");
        customer.setName("Naresh");
        customer.setPinCode("560026");
        customer.setStreetName("Richmond Town");

        ItemType item = new ItemType();
        item.setItemCode("Item01");
        item.setQuantity(100);


        CustomerOrderType customerOrder = new CustomerOrderType();
        customerOrder.setCustomer(customer);
        customerOrder.setItem(item);

        OrderType order = r.type(MediaType.APPLICATION_JSON).post(OrderType.class, customerOrder);
        assertEquals(order.getOriginatorCustomerParty().getPartyType().getPartyName().getName(), "Naresh");
        
    }
}
