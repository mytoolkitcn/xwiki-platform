package com.xpn.xwiki.objects;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.model.reference.ObjectReference;

import com.xpn.xwiki.web.Utils;

/**
 * Object reference implementation for object names generated in {@code className[objectNumber]} format. It provides
 * helper functions to extract the class name as specified by the caller, and object number. <br />
 * Accepted formats for the object name are:
 * <dl>
 * <dt>className[objectNumber]
 * <dd>interpreted as the object of class className with number objectNumber. refers the object returned by
 * XWikiDocument.getObject(String className, int objectNumber). In this case, className is obtained by calling
 * {@link #getClassName()} and object index by calling {@link #getObjectNumber()}.
 * <dt>className
 * <dd>interpreted as the first object of class className. refers the object returned by XWikiDocument.getObject(String
 * className). In this case, {@link #getObjectNumber()} will return {@code null} and {@code className} is obtained by
 * calling {@link #getClassName()}.
 * </dl>
 * 
 * @version $Id$
 */
public class BaseObjectReference extends ObjectReference
{
    private static final DocumentReferenceResolver<String> RESOLVER = Utils
        .getComponent(DocumentReferenceResolver.class);

    private static final EntityReferenceSerializer<String> SERIALIZER = Utils
        .getComponent(EntityReferenceSerializer.class);

    private static final Pattern NUMBERPATTERN = Pattern.compile("(\\\\*)\\[(\\d*)\\]$");

    /**
     * The class reference of this object.
     */
    protected DocumentReference xclassReference;

    /**
     * The number of this object.
     */
    protected Integer objectNumber;

    /**
     * Constructor which would raise exceptions if the source entity reference does not have the appropriate type or
     * parent, etc.
     * 
     * @param reference the raw reference to build this object reference from
     */
    public BaseObjectReference(EntityReference reference)
    {
        super(reference);
    }

    /**
     * Builds an indexed object reference for the object of class {@code className} with index {@code objectNumber} in
     * the document referenced by {@code parent}.
     * 
     * @param className the name of the class of the object
     * @param objectNumber the number of the object in the document, or {@code null} if the default object should be
     *            referenced
     * @param parent reference to the parent document where the object is
     */
    public BaseObjectReference(DocumentReference classReference, Integer objectNumber, DocumentReference parent)
    {
        // FIXME: Not very nice having to serialize classReference/objectNumber and then parse it in #setName
        super(toName(classReference, objectNumber), parent);
    }

    private static String toName(DocumentReference classReference, Integer objectNumber)
    {
        String name = SERIALIZER.serialize(classReference);

        if (objectNumber != null) {
            StringBuilder builder = new StringBuilder(name);
            builder.append('[');
            builder.append(objectNumber);
            builder.append(']');
            name = builder.toString();
        } else {
            Matcher matcher = NUMBERPATTERN.matcher(name);
            if (matcher.find()) {
                if (matcher.group(1).length() % 2 == 0) {
                    StringBuilder builder = new StringBuilder(name);
                    builder.insert(matcher.start(), '\\');
                    name = builder.toString();
                }
            }
        }

        return name;
    }

    /**
     * @return the reference of the class of this object, as it was set by caller. I.e. no resolving is done from the
     *         value set in the name of the object.
     */
    public DocumentReference getXClassReference()
    {
        return this.xclassReference;
    }

    /**
     * @return the number of this object among the objects of the same class in the document, as set by the caller in
     *         [objectNumber] format after the class name (i.e. no resolving is done, existence of this object is not
     *         guaranteed). If no number can be parsed (i.e. [number] cannot be parsed) this function returns
     *         {@code null} and object should be interpreted as the first object of this class in the document.
     */
    public Integer getObjectNumber()
    {
        return objectNumber;
    }

    /**
     * {@inheritDoc} <br/>
     * Overridden to always compute the class name and the object number.
     * 
     * @see org.xwiki.model.reference.EntityReference#setName(java.lang.String)
     */
    @Override
    public void setName(String name)
    {
        super.setName(name);

        String classReferenceStr;
        String objectNumberStr;

        Matcher matcher = NUMBERPATTERN.matcher(name);
        if (matcher.find()) {
            if (matcher.group(1).length() % 2 == 0) {
                classReferenceStr = name.substring(0, matcher.end(1));
                objectNumberStr = matcher.group(2);
            } else {
                classReferenceStr = name;
                objectNumberStr = null;
            }
        } else {
            classReferenceStr = name;
            objectNumberStr = null;
        }

        this.xclassReference = RESOLVER.resolve(classReferenceStr);
        if (objectNumberStr != null) {
            this.objectNumber = Integer.valueOf(objectNumberStr);
        }
    }
}
