package org.hibernate.bugs.model;

import java.util.Date;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@Entity(name = "GeneratedWithIdentity")
@Table(name = "GeneratedWithIdentitySingleTable")
public class GeneratedWithIdentity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Long id;

	public String firstname;

	public String lastname;

	@Generated(GenerationTime.ALWAYS)
	@Column(columnDefinition = "varchar(600) generated always as (firstname || ' ' || lastname) stored")
	private String fullName;

	@Temporal(value = TemporalType.TIMESTAMP)
	@Generated(GenerationTime.INSERT)
	@Column(columnDefinition = "timestamp")
	@ColumnDefault("current_timestamp")
	public Date createdAt;

	@CurrentUser.LoggedUserInsert
	public String createdBy;

	@CurrentUser.LoggedUserAlways
	public String updatedBy;

	@Generated(GenerationTime.NEVER)
	public String never;

	public GeneratedWithIdentity() {
	}

	public GeneratedWithIdentity(String firstname, String lastname) {
		this.firstname = firstname;
		this.lastname = lastname;
	}
}