package org.oasis_eu.spring.datacore.model;

import com.google.common.base.Joiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * User: schambon
 * Date: 1/2/14
 */
public class DCQueryParameters implements Iterable<DCQueryParameters.DCQueryParam> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DCQueryParameters.class);

    public static class DCQueryParam {
        String subject;
        DCOperator operator;
        DCOrdering ordering = null;
        String objectAsString = null;
        List<String> objectAsArray = null;


        DCQueryParam(String subject, DCOperator operator, DCOrdering ordering, String object) {
            this.subject = subject;
            this.operator = operator;
            this.ordering = ordering;
            this.objectAsString = object;
        }

        DCQueryParam(String subject, DCOperator operator, DCOrdering ordering, List<String> objectAsArray) {
            this.subject = subject;
            this.operator = operator;
            this.ordering = ordering;
            this.objectAsArray = objectAsArray;
        }

        public String getSubject() {
            return subject;
        }

        public DCOperator getOperator() {
            return operator;
        }

        public String getObject() {
            StringBuilder builder = new StringBuilder();

            if ((objectAsString == null || "".equals(objectAsString)) && objectAsArray == null) {
                // nothing
            } else if (objectAsString != null) {
                builder.append("\"" + objectAsString + "\"");
            } else if (objectAsArray != null) {
                Joiner joiner = Joiner.on("\",\"");
                builder.append("[\"" + joiner.join(objectAsArray) + "\"]");
            } else {
                LOGGER.error("Cannot serialize parameter that has neither string nor stringarray object");
                return "";
            }

            if (ordering != null) {
                builder.append(ordering.representation());
            }

            return builder.toString();
        }


    }

    private List<DCQueryParam> params = new ArrayList<>();

    public DCQueryParameters() {}

    public DCQueryParameters(String subject, DCOrdering order) {
        params.add(new DCQueryParam(subject, DCOperator.EQ, order, ""));
    }

    public DCQueryParameters(String subject, DCOrdering order, DCOperator operator, String object) {
        params.add(new DCQueryParam(subject, operator, order, object));
    }

    public DCQueryParameters(String subject, DCOperator operator, String object) {
        params.add(new DCQueryParam(subject, operator, null, object));
    }
    public DCQueryParameters(String subject, DCOperator operator, List<String> object) {
        params.add(new DCQueryParam(subject, operator, null, object));
    }

    public DCQueryParameters and(String subject, DCOperator operator, String object) {
        params.add(new DCQueryParam(subject, operator, null, object));
        return this;
    }
    public DCQueryParameters and(String subject, DCOperator operator, List<String> object) {
        params.add(new DCQueryParam(subject, operator, null, object));
        return this;
    }
    public DCQueryParameters and(String subject, DCOrdering order) {
        params.add(new DCQueryParam(subject, DCOperator.EQ, order, ""));
        return this;
    }

    public DCQueryParameters and(String subject, DCOrdering order, DCOperator operator, String object) {
        params.add(new DCQueryParam(subject, operator, order, object));
        return this;
    }


    @Override
    public Iterator<DCQueryParam> iterator() {
        return params.iterator();
    }
}
