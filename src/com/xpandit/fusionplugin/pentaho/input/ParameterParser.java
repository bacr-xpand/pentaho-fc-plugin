/*
 * PentahoFCPlugin Project
 * 
 * Copyright (C) 2012 Xpand IT.
 * 
 * This software is proprietary.
 */
package com.xpandit.fusionplugin.pentaho.input;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.json.JSONException;
import org.json.JSONObject;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;

import com.xpandit.fusionplugin.exception.InvalidParameterException;

/**
 * 
 * Class that parses URL\JSON requests to the component, obtaining all the necessary values.
 * 
 * @author rplp
 * @version $Revision: 666 $
 * 
 */
public class ParameterParser {
    /**
     * Encoding in use
     */
    private static final String ENCODING = "UTF-8";

    /**
     * Parameters obtained from the platform call.
     */
    private Map<String, IParameterProvider> parameterProviders = null;

    /**
     * Parameters values after parsing.
     */
    private TreeMap<String, Object> parameters = null;

    /**
     * Constructor for the class.
     * 
     * @param parameterProviders Parameters obtained from platform call.
     * @throws UnsupportedEncodingException
     */
    public ParameterParser(Map<String, IParameterProvider> parameterProviders) throws InvalidParameterException {
        this.parameterProviders = parameterProviders;

        // inialize object
        parameters = new TreeMap<String, Object>();

        // get correct IParameterProvider
        IParameterProvider requestParams = parameterProviders.get(IParameterProvider.SCOPE_REQUEST);

        if (requestParams.hasParameter("json")) {
            initializeParametersJson(requestParams);
        } else {
            initializeParametersUrl(requestParams);
        }

    }

    /**
     * Go through all the url request parameters and add them to property manager
     * 
     * @param requestParams Parameters obtained.
     * @throws InvalidParameterException When an unsuported encoding is found.
     */
    private void initializeParametersUrl(IParameterProvider requestParams) throws InvalidParameterException {
        // add file location parameters
        if (parameterProviders.get("path").getParameter("file") != null) {
            RepositoryFile file = (RepositoryFile) parameterProviders.get("path").getParameter("file");
            parameters.put("xFusionPath", file.getPath());
        }

        // add URL parameters
        @SuppressWarnings("unchecked")
        Iterator<Object> parameterNames = requestParams.getParameterNames();
        try {
            for (Iterator<Object> parameterIterator = parameterNames; parameterIterator.hasNext();) {
                String parameterKey = (String) parameterIterator.next();

                // process all the elements for a parameter
                String[] parameterArray = requestParams.getStringArrayParameter(parameterKey, null);
                for (int i = 0; i < parameterArray.length; ++i) {
                    parameterArray[i] = URLDecoder.decode(parameterArray[i].replaceAll("\\+", "%2B"), ENCODING);
                }

                // if only one element set as a string
                if (parameterArray.length == 1)
                    parameters.put(parameterKey.trim(), parameterArray[0]);
                else
                    // otherwise place the whole array
                    parameters.put(parameterKey.trim(), parameterArray);
            }
        } catch (UnsupportedEncodingException ex) {
            throw new InvalidParameterException("Unsupported Encoding Exception");
        }
    }

    /**
     * Go through all properties inside the "json" parameter and add them to property manager
     * 
     * @param requestParams Parameters obtained.
     * @throws InvalidParameterException When an unsuported encoding is found.
     */
    private void initializeParametersJson(IParameterProvider requestParams) throws InvalidParameterException {
        String jsonContent = (String) requestParams.getParameter("json");

        try {
            JSONObject jsonObject = new JSONObject(jsonContent);
            Iterator<?> iter = jsonObject.keys();
            while (iter.hasNext()) {
                String parameterKey = (String) iter.next();
                // TODO encoding is required??
                String parameteValue = jsonObject.getString(parameterKey);
                parameters.put(parameterKey, parameteValue);
            }
        } catch (JSONException ex) {
            throw new InvalidParameterException("Unsupported Encoding Exception");
        }
    }

    /**
     * 
     * Method that parses the URL call in order to identify the correspoding operation
     * 
     * @param pathString Url call that was made to the content generator.
     * @return method The corresponding operation.
     */
    public String extractMethod() {

        IParameterProvider pathParams = parameterProviders.get("path");
        String pathString = pathParams.getStringParameter("path", null);

        if (pathString == null) {
            return null;
        } else if (pathString.contains("clearCache")) {
            return "clearCache";
        } else if (pathString.contains("dataStream")) {
            return "dataStream";
        } else if (pathString.contains("checkVersions")) {
            return "checkVersions";
        } else if (pathString.contains("renderChartExternalData")) {
            return "renderChartExternalData";
        } else {
            return null;
        }
    }

    /**
     * Get all parameters.
     * 
     * @return All parameters.
     */
    public TreeMap<String, Object> getParameters() {
        return parameters;
    }

    /**
     * Obtain a specific parameter.
     * 
     * @param name The parameter name.
     * @return The parameter value.
     */
    public Object getParameters(String name) {
        return parameters.get(name);
    }
    
    public void putParameter(String key, Object value) {
    	parameters.put(key, value);
    }
}
