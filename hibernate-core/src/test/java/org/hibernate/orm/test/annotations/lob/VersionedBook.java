/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.orm.test.annotations.lob;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

/**
 * @author Gail Badner
 */
@Entity
@Table(name = "ver_lob_book")
public class VersionedBook extends AbstractBook{
	private Integer id;
	private Integer version;

	@Id
	@GeneratedValue
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@Version
	@Column(name = "verx")  // NUODB ver is a reserved word until V8
	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer i) {
		version = i;
	}
}
