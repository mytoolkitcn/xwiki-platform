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
package org.xwiki.mail.internal;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;

import org.apache.velocity.VelocityContext;
import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.component.annotation.Component;
import org.xwiki.mail.MimeBodyPartFactory;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.velocity.VelocityManager;
import org.xwiki.velocity.XWikiVelocityException;

import com.xpn.xwiki.api.Attachment;

/**
 * Creates an Body Part from a Document Reference pointing to a Document containing an XWiki.Mail XObject (the first one
 * found is used).
 *
 * @version $Id$
 * @since 6.1RC1
 */
@Component
@Named("xwiki/template")
@Singleton
public class TemplateMimeBodyPartFactory extends AbstractMimeBodyPartFactory<DocumentReference>
{
    private static final EntityReference MAIL_CLASS =
            new EntityReference("Mail", EntityType.DOCUMENT, new EntityReference("XWiki", EntityType.SPACE));

    @Inject
    private DocumentAccessBridge documentBridge;

    @Inject
    private EntityReferenceSerializer<String> serializer;

    @Inject
    private VelocityManager velocityManager;

    @Inject
    @Named("current")
    private DocumentReferenceResolver<EntityReference> resolver;

    @Inject
    @Named("text/html")
    private MimeBodyPartFactory<String> htmlBodyPartFactory;

    @Override
    public MimeBodyPart create(DocumentReference documentReference, Map<String, Object> parameters)
        throws MessagingException
    {
        Map<String, String> velocityVariables = (Map<String, String>) parameters.get("velocityVariables");
        VelocityContext velocityContext = createVelocityContext(velocityVariables);

        DocumentReference mailClassReference = this.resolver.resolve(MAIL_CLASS);
        String templateFullName = this.serializer.serialize(documentReference);

        String textContent =
                evaluateProperty(velocityContext, documentReference, mailClassReference, templateFullName, "text");
        String htmlContent =
                evaluateProperty(velocityContext, documentReference, mailClassReference, templateFullName, "html");

        Map<String, Object> htmlParameters = new HashMap<>();
        htmlParameters.put("alternate", textContent);

        String attachmentProperty = "attachments";
        List<Attachment> attachments = (List<Attachment>) parameters.get(attachmentProperty);
        if (attachments != null) {
            htmlParameters.put(attachmentProperty, attachments);
        }
        return this.htmlBodyPartFactory.create(htmlContent, htmlParameters);
    }

    /**
     * @return the xproperty's content evaluated with Velocity, using the passed Velocity Context
     */
    private String evaluateProperty(VelocityContext velocityContext, DocumentReference documentReference,
            DocumentReference mailClassReference, String templateFullName, String property) throws MessagingException
    {
        String content = this.documentBridge.getProperty(documentReference, mailClassReference, property).toString();
        try {
            StringWriter writer = new StringWriter();
            this.velocityManager.getVelocityEngine().evaluate(velocityContext, writer, templateFullName, content);
            return writer.toString();
        } catch (XWikiVelocityException e) {
            throw new MessagingException(String.format("Failed to evaluate property [%s] for Document reference [%s]",
                property, documentReference), e);
        }
    }

    /**
     * @return a Velocity Context created from the passed properties
     */
    private VelocityContext createVelocityContext(Map<String, String> data)
    {
        VelocityContext velocityContext = new VelocityContext();
        if (data != null) {
            for (Map.Entry<String, String> header : data.entrySet()) {
                velocityContext.put(header.getKey(), header.getValue());
            }
        }

        return velocityContext;
    }
}