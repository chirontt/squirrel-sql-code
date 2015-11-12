package org.squirrelsql.session.sql;

public class WordBoundaryCheck
{
   public static final char[] STOP_AT = new char[]{'.', '(', ')', '\'', '\n', ',', '=', '<', '>'};

   public static boolean isToStopAt(char toCheck, char former)
   {
      if (isInStopAtArray(former) || isInStopAtArray(toCheck))
      {
         return true;
      }
      else if (false == Character.isWhitespace(former) && Character.isWhitespace(toCheck) ||
            Character.isWhitespace(former) && false == Character.isWhitespace(toCheck))
      //     else if(Character.isWhitespace(former) && false == Character.isWhitespace(toCheck))
      {
         return true;
      }

      return false;
   }

   private static boolean isInStopAtArray(char toCheck)
   {
      for (int i = 0; i < STOP_AT.length; i++)
      {
         if (toCheck == STOP_AT[i])
         {
            return true;
         }
      }

      return false;

   }

   public static boolean isInStopAtArrayOrWhiteSpace(char c)
   {
      return Character.isWhitespace(c) ||  isInStopAtArray(c);
   }
}
