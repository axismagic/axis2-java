/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.axis2.jaxws.spi;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.Endpoint;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.spi.ServiceDelegate;
import javax.xml.ws.wsaddressing.W3CEndpointReference;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.addressing.EndpointReferenceHelper;
import org.apache.axis2.addressing.metadata.ServiceName;
import org.apache.axis2.addressing.metadata.WSDLLocation;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.addressing.util.EndpointReferenceUtils;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.server.endpoint.EndpointImpl;
import org.apache.axis2.util.XMLUtils;
import org.w3c.dom.Element;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.List;

public class Provider extends javax.xml.ws.spi.Provider {
	private static final Element[] ZERO_LENGTH_ARRAY = new Element[0];

    @Override
    public Endpoint createAndPublishEndpoint(String s, Object obj) {
        Endpoint ep = new EndpointImpl(obj);
        ep.publish(s);
        return ep;
    }

    @Override
    public Endpoint createEndpoint(String binding, Object obj) {
        return new EndpointImpl(obj);
    }

    @Override
    public ServiceDelegate createServiceDelegate(URL url, QName qname, Class clazz) {
        return new org.apache.axis2.jaxws.spi.ServiceDelegate(url, qname, clazz);
    }

    @Override
    public W3CEndpointReference createW3CEndpointReference(String address,
            QName serviceName,
            QName portName,
            List<Element> metadata,
            String wsdlDocumentLocation,
            List<Element> referenceParameters) {
        String addressingNamespace =
        	EndpointReferenceUtils.getAddressingNamespace(W3CEndpointReference.class);    	
        org.apache.axis2.addressing.EndpointReference axis2EPR =
        	EndpointReferenceUtils.createAxis2EndpointReference(address, serviceName, portName, wsdlDocumentLocation, addressingNamespace);
        
        W3CEndpointReference w3cEPR = null;
        
        try {
        	EndpointReferenceUtils.addMetadata(axis2EPR, metadata.toArray(ZERO_LENGTH_ARRAY));
        	EndpointReferenceUtils.addReferenceParameters(axis2EPR, referenceParameters.toArray(ZERO_LENGTH_ARRAY));
        	
            w3cEPR =
                (W3CEndpointReference) EndpointReferenceUtils.convertFromAxis2(axis2EPR, addressingNamespace);
        }
        catch (Exception e) {
            //TODO NLS enable.
            throw ExceptionFactory.makeWebServiceException("A problem occured during the creation of an endpoint reference. See the nested exception for details.", e);
        }
        
        return w3cEPR;
    }

    @Override
    public <T> T getPort(EndpointReference jaxwsEPR, Class<T> sei, WebServiceFeature... features) {
        if (jaxwsEPR == null) {
            //TODO NLS enable.
            throw ExceptionFactory.makeWebServiceException("The endpoint reference cannot be null.");
        }
        
        if (sei == null) {
            throw ExceptionFactory.makeWebServiceException(
                    Messages.getMessage("getPortInvalidSEI", jaxwsEPR.toString(), "null"));
        }
        
        org.apache.axis2.addressing.EndpointReference axis2EPR =
            EndpointReferenceUtils.createAxis2EndpointReference("");
        String addressingNamespace = null;
        
        try {
            addressingNamespace = EndpointReferenceUtils.convertToAxis2(axis2EPR, jaxwsEPR);
        }
        catch (Exception e) {
            //TODO NLS enable.
            throw ExceptionFactory.makeWebServiceException("Invalid endpoint reference.", e);
        }
        
        org.apache.axis2.jaxws.spi.ServiceDelegate serviceDelegate = null;
        
        try {
            ServiceName serviceName =
            	EndpointReferenceHelper.getServiceNameMetadata(axis2EPR, addressingNamespace);
            WSDLLocation wsdlLocation =
            	EndpointReferenceHelper.getWSDLLocationMetadata(axis2EPR, addressingNamespace);
            URL wsdlLocationURL = null;
            
            if (wsdlLocation.getLocation() != null)
            	wsdlLocationURL = new URL(wsdlLocation.getLocation());
            else
            	wsdlLocationURL = new URL(axis2EPR.getAddress() + "?wsdl");
            
            serviceDelegate =
            	new org.apache.axis2.jaxws.spi.ServiceDelegate(wsdlLocationURL, serviceName.getName(), Service.class);
        }
        catch (Exception e) {
            //TODO NLS enable.
            throw ExceptionFactory.makeWebServiceException("An error occured updating the endpoint", e);
        }

        return serviceDelegate.getPort(axis2EPR, addressingNamespace, sei, features);
    }

    @Override
    public EndpointReference readEndpointReference(Source eprInfoset) {
        EndpointReference jaxwsEPR = null;

        try {
            Transformer xformer = TransformerFactory.newInstance().newTransformer();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            xformer.transform(eprInfoset, new StreamResult(baos));
            
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            OMElement eprElement = (OMElement) XMLUtils.toOM(bais);
            org.apache.axis2.addressing.EndpointReference axis2EPR =
                EndpointReferenceUtils.createAxis2EndpointReference("");
            String addressingNamespace = EndpointReferenceHelper.fromOM(axis2EPR, eprElement);
            
            jaxwsEPR = EndpointReferenceUtils.convertFromAxis2(axis2EPR, addressingNamespace);
        }
        catch (Exception e) {
            //TODO NLS enable.
            throw ExceptionFactory.makeWebServiceException("A problem occured during the creation of an endpoint reference. See the nested exception for details.", e);
        }
        
        return jaxwsEPR;
    }
}