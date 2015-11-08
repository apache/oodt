/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.oodt.cas.filemgr.validation;

//OODT imports
import org.apache.oodt.cas.filemgr.structs.Element;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.exceptions.ValidationLayerException;

//JDK imports
import java.util.List;

/**
 * @author mattmann
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * The Validation Layer for the File Manager, allowing {@link Element}s to be
 * mapped to {@link ProductType}s.
 * </p>
 * 
 */
public interface ValidationLayer {

    /**
     * <p>
     * Adds the <code>Element</code> to the ValidationLayer.
     * </p>
     * 
     * @param element
     *            The element to add.
     * @throws ValidationLayerException
     *             If any error occurs during the add.
     */
    void addElement(Element element) throws ValidationLayerException;

    /**
     * <p>
     * Modifies an existing {@link Element} in the ValidationLayer.
     * </p>
     * 
     * @param element
     *            The new {@link Element} data to update.
     * @throws ValidationLayerException
     *             If any error occurs.
     */
    void modifyElement(Element element) throws ValidationLayerException;

    /**
     * <p>
     * Removes a metadata {@link Element} from the ValidationLayer.
     * 
     * @param element
     *            The element to remove.
     * @throws ValidationLayerException
     *             If any error occurs.
     */
    void removeElement(Element element) throws ValidationLayerException;

    /**
     * <p>
     * Adds the specified <code>element</code> to the {@link ProductType}
     * specified by its <code>productTypeId</code>.
     * 
     * @param type
     *            The {@link ProductType} to associate the metadata Element
     *            with.
     * @param element
     *            The {@link Element} to associate with the ProductType.
     * @throws ValidationLayerException
     *             If any error occurs.
     */
    void addElementToProductType(ProductType type, Element element)
            throws ValidationLayerException;

    /**
     * <p>
     * Removes a metadata {@link Element} from the specified
     * <code>productTypeId</code>.
     * </p>
     * 
     * @param type
     *            The {@link ProductType} to remove the association of the
     *            specified <code>element</code> with.
     * @param element
     *            The element whose association will be removed from the
     *            specified ProductType.
     * @throws ValidationLayerException
     *             If any error occurs.
     */
    void removeElementFromProductType(ProductType type, Element element)
            throws ValidationLayerException;

    /**
     * <p>
     * Returns a {@link List} of {@link Element}s corresponding to the given
     * ProductType.
     * 
     * @param type
     *            The product type to retrieve the metadata {@link Element}s
     *            for.
     * @return A {@link List} of {@link Element}s corresponding to the given
     *         ProductType.
     * @throws ValidationLayerException
     *             If any error occurs.
     */
    List<Element> getElements(ProductType type)
            throws ValidationLayerException;

    /**
     * @return A {@link List} of all the metadata {@link Element}s in the
     *         ValidationLayer.
     * @throws ValidationLayerException
     *             If any error occurs.
     */
    List<Element> getElements() throws ValidationLayerException;

    /**
     * Gets an element by its String identifier.
     * 
     * @param elementId
     *            The String identifier of the {@link Element} to get.
     * @return An {@link Element} by its String identifier.
     * @throws ValidationLayerException
     *             If any error occurs.
     */
    Element getElementById(String elementId)
            throws ValidationLayerException;

    /**
     * Gets an element by its String name.
     * 
     * @param elementName
     *            The String name of the {@link Element} to get.
     * @return An {@link Element} by its String name.
     * @throws ValidationLayerException
     *             If any error occurs.
     */
    Element getElementByName(String elementName)
            throws ValidationLayerException;

}
