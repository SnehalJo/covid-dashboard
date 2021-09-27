package com.covid.dashboard.model;

import java.util.Objects;

public class Key {

    private String provinceState;
    private String countryRegion;
	public String getProvinceState() {
		return provinceState;
	}
	public void setProvinceState(String provinceState) {
		this.provinceState = provinceState;
	}
	public String getCountryRegion() {
		return countryRegion;
	}
	public void setCountryRegion(String countryRegion) {
		this.countryRegion = countryRegion;
	}
	@Override
	public int hashCode() {
		return Objects.hash(countryRegion, provinceState);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Key other = (Key) obj;
		return Objects.equals(countryRegion, other.countryRegion) &&  Objects.equals(provinceState, other.provinceState);
	}
}
