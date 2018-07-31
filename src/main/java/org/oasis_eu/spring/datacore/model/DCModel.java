package org.oasis_eu.spring.datacore.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;
import java.util.List;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DCModel implements Comparable<DCModel> {

    @JsonProperty("@id")
    private URI id;

    @JsonProperty("dcmo:name")
    private String name;

    @JsonProperty("o:version")
    private Integer version;

    @JsonProperty("dcmo:pointOfViewAbsoluteName")
    private String project;

    @JsonProperty("dcmo:globalFields")
    private List<DcModelField> globalFields;

    @JsonProperty("dcmo:fields")
    private List<DcModelField> fields; 
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public URI getId() {
        return id;
    }

    public void setId(URI id) {
        this.id = id;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getProject() { return project; }

    public void setProject(String project) { this.project = project; }

    public List<DcModelField> getFields() {
        return fields;
    }

    public void setFields(List<DcModelField> fields) {
        this.fields = fields;
    }
    
    public List<DcModelField> getGlobalFields () {
        return globalFields;
    }
    
    public void setGlobalFields(List<DcModelField> globalFields) {
        this.globalFields = globalFields;
    }
    
    @Override
    public String toString() {
        return "DCModel{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", version=" + version +
                ", projet='" + project + '\'' +
                '}';
    }

    @Override
    public int compareTo(DCModel otherModel) {
        return name.compareTo(otherModel.getName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DCModel model = (DCModel) o;
        return Objects.equals(id, model.id) &&
            Objects.equals(version, model.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, version);
    }

    public static class DcModelField {

        @JsonProperty("dcmf:name")
        private String name;

        @JsonProperty("dcmf:documentation")
        private String documentation;
        
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDocumentation() {
            return documentation;
        }

        public void setDocumentation (String documentation) {
            this.documentation = documentation;
        }
        
        
        public DcModelField() {
        }
    }
}
