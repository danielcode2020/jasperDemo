package com.example.jasperdemo.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.util.Objects;

@XmlAccessorType(XmlAccessType.FIELD)
public class Resource {
    private String creationDate;
    private String description;
    private String label;
    private String permissionMask;
    private String updateDate;
    private String uri;
    private String version;
    private String resourceType;

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getPermissionMask() {
        return permissionMask;
    }

    public void setPermissionMask(String permissionMask) {
        this.permissionMask = permissionMask;
    }

    public String getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(String updateDate) {
        this.updateDate = updateDate;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Resource resource = (Resource) o;
        return Objects.equals(creationDate, resource.creationDate) && Objects.equals(description, resource.description) && Objects.equals(label, resource.label) && Objects.equals(permissionMask, resource.permissionMask) && Objects.equals(updateDate, resource.updateDate) && Objects.equals(uri, resource.uri) && Objects.equals(version, resource.version) && Objects.equals(resourceType, resource.resourceType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(creationDate, description, label, permissionMask, updateDate, uri, version, resourceType);
    }

    @Override
    public String toString() {
        return "Resource{" +
                "creationDate='" + creationDate + '\'' +
                ", description='" + description + '\'' +
                ", label='" + label + '\'' +
                ", permissionMask='" + permissionMask + '\'' +
                ", updateDate='" + updateDate + '\'' +
                ", uri='" + uri + '\'' +
                ", version='" + version + '\'' +
                ", resourceType='" + resourceType + '\'' +
                '}';
    }
}