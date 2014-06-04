package org.subsurface.model;

/**
 * Search criterii used.
 * @author Aurelien PRALONG
 *
 */
public class SearchCriterii {

	private String name;
	private long startDate;
	private long endDate;
	private boolean pendingOnly;

	public SearchCriterii() {
		this.name = null;
		this.startDate = 0;
		this.endDate = Long.MAX_VALUE;
		this.pendingOnly = false;
	}

	public SearchCriterii(String name, long startDate, long endDate, boolean pendingOnly) {
		this.name = name;
		this.startDate = startDate;
		this.endDate = endDate;
		this.pendingOnly = pendingOnly;
	}

	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getStartDate() {
		return startDate;
	}

	public void setStartDate(long startDate) {
		this.startDate = startDate;
	}

	public long getEndDate() {
		return endDate;
	}

	public void setEndDate(long endDate) {
		this.endDate = endDate;
	}

	public boolean isPendingOnly() {
		return pendingOnly;
	}

	public void setPendingOnly(boolean pendingOnly) {
		this.pendingOnly = pendingOnly;
	}
}
