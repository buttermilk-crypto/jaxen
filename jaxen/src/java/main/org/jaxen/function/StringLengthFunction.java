/*
 * $Header$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 *
 * Copyright (C) 2000-2002 bob mcwhirter & James Strachan.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions, and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions, and the disclaimer that follows 
 *    these conditions in the documentation and/or other materials 
 *    provided with the distribution.
 *
 * 3. The name "Jaxen" must not be used to endorse or promote products
 *    derived from this software without prior written permission.  For
 *    written permission, please contact license@jaxen.org.
 * 
 * 4. Products derived from this software may not be called "Jaxen", nor
 *    may "Jaxen" appear in their name, without prior written permission
 *    from the Jaxen Project Management (pm@jaxen.org).
 * 
 * In addition, we request (but do not require) that you include in the 
 * end-user documentation provided with the redistribution and/or in the 
 * software itself an acknowledgement equivalent to the following:
 *     "This product includes software developed by the
 *      Jaxen Project <http://www.jaxen.org/>."
 * Alternatively, the acknowledgment may be graphical using the logos 
 * available at http://www.jaxen.org/
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE Jaxen AUTHORS OR THE PROJECT
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 * ====================================================================
 * This software consists of voluntary contributions made by many 
 * individuals on behalf of the Jaxen Project and was originally 
 * created by bob mcwhirter <bob@werken.com> and 
 * James Strachan <jstrachan@apache.org>.  For more information on the 
 * Jaxen Project, please see <http://www.jaxen.org/>.
 * 
 * $Id$
 */


package org.jaxen.function;

import java.util.List;

import org.jaxen.Context;
import org.jaxen.Function;
import org.jaxen.FunctionCallException;
import org.jaxen.Navigator;

/**
 * <p><b>4.2</b> <code><i>number</i> string-length(<i>string</i>)</code></p> 
 * 
 * <p>
 * The <b>string-length</b> function returns the number of <strong>Unicode characters</strong>
 * in its argument. This is <strong>not</strong> necessarily 
 * the same as the number <strong>Java chars</strong>
 * in the corresponding Java string. In particular, if the Java <code>String</code>
 * contains surrogate pairs each such pair will be counted as only one character
 * by this function. If the argument is omitted, 
 * it returns the length of the string-value of the context node.
 * </p>
 * 
 * @author bob mcwhirter (bob @ werken.com)
 * @see <a href="http://www.w3.org/TR/xpath#function-string-length" target="_top">Section 
 *      4.2 of the XPath Specification</a>
 */
public class StringLengthFunction implements Function
{

    
    /**
     * Create a new <code>StringLengthFunction</code> object.
     */
    public StringLengthFunction() {}
    
    
    /**
     * <p>
     * Returns the number of Unicode characters in the string-value of the argument.
     * </p>
     * 
     * @param context the context at the point in the
     *         expression when the function is called
     * @param args a list containing the item whose string-value is to be counted.
     *     If empty, the length of the context node's string-value is returned.
     * 
     * @return a <code>Double</code> giving the number of Unicode characters
     * 
     * @throws FunctionCallException if args has more than one item
     */
    public Object call(Context context,
                       List args) throws FunctionCallException
    {
        if (args.size() == 0)
        {
            return evaluate( context.getNodeSet(),
                             context.getNavigator() );
        } 
        else if (args.size() == 1)
        {
            return evaluate( args.get(0),
                             context.getNavigator() );
        }

        throw new FunctionCallException( "string-length() requires one argument." );
    }

    /**
     * <p>
     * Returns the number of Unicode characters in the string-value of 
     * an object.
     * </p>
     * 
     * @param obj the object whose string-value is counted
     * @param nav used to calculate the string-values of the first two arguments
     * 
     * @return a <code>Double</code> giving the number of Unicode characters
     * 
     * @throws FunctionCallException if the string contains mismatched surrogates
     */
    public static Double evaluate(Object obj, Navigator nav) throws FunctionCallException
    {
        
        // could/should I push the mismnatching checks into StringFunction.evaluate()????
        String str = StringFunction.evaluate( obj, nav );
        // String.length() counts UTF-16 code points; not Unicode characters
        char[] data = str.toCharArray();
        int length = 0;
        for (int i = 0; i < data.length; i++) {
            char c = data[i];
            length++;
            // if this is a high surrogate; assume the next character is
            // is a low surrogate and skip it
            if (c >= 0xD800) {
                try {
                    char low = data[i+1];
                    if (low < 0xDC00 || low > 0xDFFF) {
                        throw new FunctionCallException("Bad surrogate pair in string " + str);
                    }
                    i++; // increment past low surrogate
                }
                catch (ArrayIndexOutOfBoundsException ex) {
                    throw new FunctionCallException("Bad surrogate pair in string " + str);
                }
            }
        }
        return new Double(length);
    }
    
}
