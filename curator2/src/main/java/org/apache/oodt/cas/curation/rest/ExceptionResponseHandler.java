package org.apache.oodt.cas.curation.rest;

import javax.ws.rs.core.Response;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Helper class to build response for thrown exception
 * 
 * @author starchmd
 */
public class ExceptionResponseHandler {
    /*
     * Static initializer to get GSON converter
     */
    private static Gson gson;
    {
        gson = new GsonBuilder().create();
    }
    /**
     * Recurse down an exception causality tree
     * @param e -exception
     * @return exception info regarding this exception
     */
    public static ExceptionInfo recurseExceptionTree(Throwable e) {
        ExceptionInfo info = new ExceptionInfo();
        info.name = e.getClass().getName();
        info.message = e.getLocalizedMessage();
        info.stack = e.getStackTrace();
        info.cause = recurseExceptionTree(e.getCause());
        return info;
    }
    /**
     * Return a response that wraps an exception
     * @return
     */
    public static Response BuildExceptionResponse(Exception e) {
        ExceptionInfo info = recurseExceptionTree(e);
        return Response.serverError().entity(gson.toJson(info)).build();
    }
    /**
     * Information regarding an exception
     * @author starchmd
     */
    static class ExceptionInfo {
        String name;
        String message;
        ExceptionInfo cause;
        StackTraceElement[] stack;
    }
}
