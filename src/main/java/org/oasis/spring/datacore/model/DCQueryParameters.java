package org.oasis.spring.datacore.model;

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
        String objectAsString;
        List<String> objectAsArray;

        DCQueryParam(String subject, DCOperator operator, String object) {
            this.subject = subject;
            this.operator = operator;
            this.objectAsString = object;
        }

        DCQueryParam(String subject, DCOperator operator, List<String> objectAsArray) {
            this.subject = subject;
            this.operator = operator;
            this.objectAsArray = objectAsArray;
        }

        public String getSubject() {
            return subject;
        }

        public DCOperator getOperator() {
            return operator;
        }

        public String getObject() {
            if (objectAsString != null) {
                return "\"" + objectAsString + "\"";
            } else if (objectAsArray != null) {
                Joiner joiner = Joiner.on("\",\"");
                return "[\"" + joiner.join(objectAsArray) + "\"]";
            } else {
                LOGGER.error("Cannot serialize parameter that has neither string nor stringarray object");
                return "";
            }
        }


    }

    private List<DCQueryParam> params = new ArrayList<>();

    public DCQueryParameters() {}

    public DCQueryParameters(String subject, DCOperator operator, String object) {
        params.add(new DCQueryParam(subject, operator, object));
    }
    public DCQueryParameters(String subject, DCOperator operator, List<String> object) {
        params.add(new DCQueryParam(subject, operator, object));
    }

    public DCQueryParameters and(String subject, DCOperator operator, String object) {
        params.add(new DCQueryParam(subject, operator, object));
        return this;
    }
    public DCQueryParameters and(String subject, DCOperator operator, List<String> object) {
        params.add(new DCQueryParam(subject, operator, object));
        return this;
    }

    @Override
    public Iterator<DCQueryParam> iterator() {
        return params.iterator();
    }
}
