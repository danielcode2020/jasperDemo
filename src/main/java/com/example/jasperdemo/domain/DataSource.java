package com.example.jasperdemo.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.io.Serializable;

@Entity
@Table(name = "data_source")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class DataSource implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "label", nullable = false)
    private String label;

    @NotNull
    @Column(name = "driver_class", nullable = false)
    private String driverClass;

    @NotNull
    @Column(name = "password", nullable = false)
    private String password;

    @NotNull
    @Column(name = "username", nullable = false)
    private String username;

    @NotNull
    @Column(name = "connection_url", nullable = false)
    private String connectionUrl;

    @NotNull
    @Column(name = "uri", nullable = false)
    private String uri;


    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public DataSource id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLabel() {
        return this.label;
    }

    public DataSource label(String label) {
        this.setLabel(label);
        return this;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDriverClass() {
        return this.driverClass;
    }

    public DataSource driverClass(String driverClass) {
        this.setDriverClass(driverClass);
        return this;
    }

    public void setDriverClass(String driverClass) {
        this.driverClass = driverClass;
    }

    public String getPassword() {
        return this.password;
    }

    public DataSource password(String password) {
        this.setPassword(password);
        return this;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return this.username;
    }

    public DataSource username(String username) {
        this.setUsername(username);
        return this;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getConnectionUrl() {
        return this.connectionUrl;
    }

    public DataSource connectionUrl(String connectionUrl) {
        this.setConnectionUrl(connectionUrl);
        return this;
    }

    public void setConnectionUrl(String connectionUrl) {
        this.connectionUrl = connectionUrl;
    }


    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DataSource)) {
            return false;
        }
        return id != null && id.equals(((DataSource) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "DataSource{" +
                "id=" + getId() +
                ", label='" + getLabel() + "'" +
                ", driverClass='" + getDriverClass() + "'" +
                ", password='" + getPassword() + "'" +
                ", username='" + getUsername() + "'" +
                ", connectionUrl='" + getConnectionUrl() + "'" +
                ", uri='" + getUri() + "'" +
                "}";
    }
}
