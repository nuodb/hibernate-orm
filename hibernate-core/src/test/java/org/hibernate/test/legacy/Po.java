/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */

//$Id: Po.java 4599 2004-09-26 05:18:27Z oneovthafew $
package org.hibernate.test.legacy;
import java.util.List;
import java.util.Set;


/**
 *
 */
public class Po {
	private long id;

	// TODO: NuoDB: 21-Jan-19 - Provide default for string properties
	// NuoDB 3.2 or earlier requires actual default values
	// From NuoDB 3.3 null is the default default-value
	private String value = "";

	private Set set;
	private List list;
	private Top top;
	private Lower lower;
	/**
	 * Returns the id.
	 * @return long
	 */
	public long getId() {
		return id;
	}
	
	/**
	 * Returns the value.
	 * @return String
	 */
	public String getValue() {
		return value;
	}
	
	/**
	 * Sets the id.
	 * @param id The id to set
	 */
	public void setId(long id) {
		this.id = id;
	}
	
	/**
	 * Sets the value.
	 * @param value The value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}
	
	/**
	 * Returns the set.
	 * @return Set
	 */
	public Set getSet() {
		return set;
	}
	
	/**
	 * Sets the set.
	 * @param set The set to set
	 */
	public void setSet(Set set) {
		this.set = set;
	}
	
	/**
	 * Returns the list.
	 * @return List
	 */
	public List getList() {
		return list;
	}
	
	/**
	 * Sets the list.
	 * @param list The list to set
	 */
	public void setList(List list) {
		this.list = list;
	}
	
	public Lower getLower() {
		return lower;
	}

	public Top getTop() {
		return top;
	}

	public void setLower(Lower lower) {
		this.lower = lower;
	}

	public void setTop(Top top) {
		this.top = top;
	}

}






