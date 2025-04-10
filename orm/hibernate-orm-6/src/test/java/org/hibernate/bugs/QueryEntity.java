package org.hibernate.bugs;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.util.List;
import java.util.UUID;

@Entity
public class QueryEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator",
            parameters = {
                    @Parameter(
                            name = "uuid_gen_strategy_class",
                            value = "org.hibernate.id.uuid.CustomVersionOneStrategy"
                    )
            }
    )
    UUID id;

    @JoinColumn(name = "dependency_id")
    @ManyToOne(cascade = CascadeType.PERSIST)
    private QueryEntity dependency;


    @OneToMany(mappedBy = "dependency", cascade = CascadeType.ALL)
    private List<QueryEntity> requiredBy;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public QueryEntity getDependency() {
        return dependency;
    }

    public void setDependency(QueryEntity dependency) {
        this.dependency = dependency;
    }

    public List<QueryEntity> getRequiredBy() {
        return requiredBy;
    }

    public void setRequiredBy(List<QueryEntity> requiredBy) {
        this.requiredBy = requiredBy;
    }
}