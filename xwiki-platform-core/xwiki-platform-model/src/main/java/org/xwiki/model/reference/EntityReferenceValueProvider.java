/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.model.reference;

import org.xwiki.component.annotation.Role;
import org.xwiki.model.EntityType;

/**
 * Return default values for specified Entity Reference types. Useful when an Entity Reference value has not been
 * specified and we need to find a default value for it (the default value returned will depend on the implementations,
 * some will return default values defined in the XWiki configuration, others will return values taken from the current
 * document reference, etc).
 * 
 * @version $Id$
 * @since 2.3M1
 */
@Role
public interface EntityReferenceValueProvider
{
    /**
     * @param type the entity reference type for which to get the default value
     * @return the default value for the passed type
     */
    String getDefaultValue(EntityType type);
}