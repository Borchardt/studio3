/**
 * This file Copyright (c) 2005-2010 Aptana, Inc. This program is
 * dual-licensed under both the Aptana Public License and the GNU General
 * Public license. You may elect to use one or the other of these licenses.
 * 
 * This program is distributed in the hope that it will be useful, but
 * AS-IS and WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, TITLE, or
 * NONINFRINGEMENT. Redistribution, except as permitted by whichever of
 * the GPL or APL you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or modify this
 * program under the terms of the GNU General Public License,
 * Version 3, as published by the Free Software Foundation.  You should
 * have received a copy of the GNU General Public License, Version 3 along
 * with this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Aptana provides a special exception to allow redistribution of this file
 * with certain other free and open source software ("FOSS") code and certain additional terms
 * pursuant to Section 7 of the GPL. You may view the exception and these
 * terms on the web at http://www.aptana.com/legal/gpl/.
 * 
 * 2. For the Aptana Public License (APL), this program and the
 * accompanying materials are made available under the terms of the APL
 * v1.0 which accompanies this distribution, and is available at
 * http://www.aptana.com/legal/apl/.
 * 
 * You may view the GPL, Aptana's exception and additional terms, and the
 * APL in the file titled license.html at the root of the corresponding
 * plugin containing this source file.
 * 
 * Any modifications to this file must keep this entire header intact.
 */

package com.aptana.syncing.core.internal.model;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

import com.aptana.syncing.core.internal.model.SyncPair.Direction;
import com.aptana.syncing.core.model.ISyncItem;

/**
 * @author Max Stepanov
 *
 */
/* package */ final class SyncItem implements ISyncItem {

	protected static final ISyncItem[] EMPTY_LIST = new ISyncItem[0];

	private IPath path;
	private SyncPair syncPair;
	private ISyncItem[] childItems;
	
	/**
	 * 
	 */
	protected SyncItem(IPath path, SyncPair syncPair) {
		this.path = path;
		this.syncPair = syncPair;
	}
	
	/* package*/ void setChildItems(ISyncItem[] childItems) {
		this.childItems = childItems;
	}

	/* (non-Javadoc)
	 * @see com.aptana.syncing.core.model.ISyncItem#getChildItems()
	 */
	@Override
	public ISyncItem[] getChildItems() {
		return childItems;
	}

	/* (non-Javadoc)
	 * @see com.aptana.syncing.core.model.ISyncItem#getLeftFileInfo()
	 */
	@Override
	public IFileInfo getLeftFileInfo() {
		return syncPair.getLeftFileInfo();
	}

	/* (non-Javadoc)
	 * @see com.aptana.syncing.core.model.ISyncItem#getRightFileInfo()
	 */
	@Override
	public IFileInfo getRightFileInfo() {
		return syncPair.getRightFileInfo();
	}

	/* (non-Javadoc)
	 * @see com.aptana.syncing.core.model.ISyncItem#getName()
	 */
	@Override
	public String getName() {
		return path.lastSegment();
	}

	/* (non-Javadoc)
	 * @see com.aptana.syncing.core.model.ISyncItem#getOperation()
	 */
	@Override
	public Operation getOperation() {
		switch(syncPair.getDirection()) {
		case LEFT_TO_RIGHT:
			return Operation.LEFT_TO_RIGHT;
		case RIGHT_TO_LEFT:
			return Operation.RIGHT_TO_LEFT;
		case SAME:
		case AMBIGUOUS:
		case INCONSISTENT:
		default:
			return Operation.NONE;
		}
	}

	/* (non-Javadoc)
	 * @see com.aptana.syncing.core.model.ISyncItem#getAllowedOperations()
	 */
	@Override
	public Set<Operation> getAllowedOperations() {
		Set<Operation> set = new HashSet<Operation>();
		set.add(Operation.NONE);
		if (syncPair.getLeftFileInfo().exists() && !syncPair.getRightFileInfo().isDirectory()) {
			set.add(Operation.LEFT_TO_RIGHT);
		}
		if (syncPair.getRightFileInfo().exists() && !syncPair.getLeftFileInfo().isDirectory()) {
			set.add(Operation.RIGHT_TO_LEFT);
		}
		return set;
	}

	/* (non-Javadoc)
	 * @see com.aptana.syncing.core.model.ISyncItem#setOperation(com.aptana.syncing.core.model.ISyncItem.Operation)
	 */
	@Override
	public void setOperation(Operation operation) {
		switch (operation) {
		case LEFT_TO_RIGHT:
			syncPair.setForceDirection(Direction.LEFT_TO_RIGHT);
			break;
		case RIGHT_TO_LEFT:
			syncPair.setForceDirection(Direction.RIGHT_TO_LEFT);
			break;
		case NONE:
			syncPair.setForceDirection(Direction.NONE);
			break;
		default:
			syncPair.setForceDirection(null);
			break;
		}
	}

	/* (non-Javadoc)
	 * @see com.aptana.syncing.core.model.ISyncItem#getStatus()
	 */
	@Override
	public Status getStatus() {
		switch(syncPair.getDefaultDirection()) {
		case SAME:
			return Status.NONE;
		case LEFT_TO_RIGHT:
			return Status.LEFT_TO_RIGHT;
		case RIGHT_TO_LEFT:
			return Status.RIGHT_TO_LEFT;
		case AMBIGUOUS:
		case INCONSISTENT:
			return Status.CONFLICT;
		default:
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see com.aptana.syncing.core.model.ISyncItem#getPath()
	 */
	@Override
	public IPath getPath() {
		return path;
	}

	/* (non-Javadoc)
	 * @see com.aptana.syncing.core.model.ISyncItem#getType()
	 */
	@Override
	public Type getType() {
		if (syncPair.getDirection() == Direction.INCONSISTENT) {
			return Type.UNSUPPORTED;
		}
		if (syncPair.getLeftFileInfo().isDirectory() || syncPair.getRightFileInfo().isDirectory()) {
			return Type.FOLDER;
		}
		if (syncPair.getLeftFileInfo().getAttribute(EFS.ATTRIBUTE_SYMLINK)) {
			return Type.UNSUPPORTED;
		}
		return Type.FILE;
	}

	/* (non-Javadoc)
	 * @see com.aptana.syncing.core.model.ISyncItem#synchronize(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public boolean synchronize(IProgressMonitor monitor) throws CoreException {
		return syncPair.synchronize(monitor);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SyncItem [path=").append(path).append("]");
		return builder.toString();
	}

}
