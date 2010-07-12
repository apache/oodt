// Copyright (c) 2005, California Institute of Technology.
// ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
// 
// $Id: ActivityException.java,v 1.1 2005-01-05 18:22:26 shardman Exp $

package jpl.eda.activity;

/**
   This class is intended to be thrown as an exception in the jpl.eda.activity package.

   @author S. Hardman
   @version $Revision: 1.1 $
*/
public class ActivityException extends Exception {

   /**
      This constructor calls the {@link Exception} constructor with the same signature.
   */
   public ActivityException() {
      super();
   }


   /**
      This constructor calls the {@link Exception} constructor with the same signature.

      @param message The message associated with the exception.
   */
   public ActivityException(String message) {
      super(message);
   }


   /**
      This constructor calls the {@link Exception} constructor with the same signature.

      @param cause The exception that caused this exception.
   */
   public ActivityException(Throwable cause) {
      super(cause);
   }


   /**
      This constructor calls the {@link Exception} constructor with the same signature.

      @param message The message associated with the exception.
      @param cause The exception that caused this exception.
   */
   public ActivityException(String message, Throwable cause) {
      super(message, cause);
   }
}

