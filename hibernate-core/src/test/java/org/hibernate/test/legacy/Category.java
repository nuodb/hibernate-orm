/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */

//$Id: Category.java 5686 2005-02-12 07:27:32Z steveebersole $
package org.hibernate.test.legacy;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class Category {
	
	public static final String ROOT_CATEGORY = "/";
	public static final int ROOT_ID = 42;

	private long id;

	// TODO: NuoDB: 21-Jan-19 - Provide default for string properties
	// NuoDB 3.2 or earlier requires actual default values
	// From NuoDB 3.3 null is the default default-value
	private String name = "";

	private List subcategories = new ArrayList();
	private Assignable assignable = null;
	/**
	 * Returns the id.
	 * @return long
	 */
	public long getId() {
		return id;
	}
	
	/**
	 * Sets the id.
	 * @param id The id to set
	 */
	public void setId(long id) {
		this.id = id;
	}
	
	/**
	 * Returns the subcategories.
	 * @return List
	 */
	public List getSubcategories() {
		return subcategories;
	}
	
	/**
	 * Sets the subcategories.
	 * @param subcategories The subcategories to set
	 */
	public void setSubcategories(List subcategories) {
		this.subcategories = subcategories;
	}
	
	/**
	 * Returns the name.
	 * @return String
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Sets the name.
	 * @param name The name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	public Assignable getAssignable() {
		return assignable;
	}

	public void setAssignable(Assignable assignable) {
		this.assignable = assignable;
	}
	
	public String toString() {
		return id + ":" + name;
	}

}






