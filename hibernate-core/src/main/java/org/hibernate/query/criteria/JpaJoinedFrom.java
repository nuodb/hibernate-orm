/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.criteria;

/**
 * Exists within the hierarchy mainly to support "entity joins".
 *
 * @see JpaEntityJoin
 * @see org.hibernate.query.sqm.tree.from.SqmEntityJoin
 *
 * @author Steve Ebersole
 */
public interface JpaJoinedFrom<O,T> extends JpaFrom<O, T> {
}