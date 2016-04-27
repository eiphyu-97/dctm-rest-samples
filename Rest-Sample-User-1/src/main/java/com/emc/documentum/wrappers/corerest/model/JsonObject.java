/*
 * Copyright (c) 2014. EMC Corporation. All Rights Reserved.
 */
package com.emc.documentum.wrappers.corerest.model;

import java.util.HashMap;
import java.util.List;

import com.emc.documentum.constants.LinkRelation;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.google.common.base.Strings;

public class JsonObject {
	private String name;
	private String type;
	private String definition;
	@JsonFormat(with = { JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY,
			JsonFormat.Feature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED })
	private HashMap<String, Object> properties;
	@JsonProperty
	@JsonTypeInfo(use = Id.CLASS, defaultImpl = JsonLink.class)
	private List<JsonLink> links;

	public List<JsonLink> getLinks() {
		return links;
	}

	public void setLinks(List<JsonLink> links) {
		this.links = links;
	}

	public JsonObject() {
	}

	public JsonObject(JsonObject object) {
		this.name = object.getName();
		this.type = object.getType();
		this.definition = object.getDefinition();
		this.properties = object.getProperties();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDefinition() {
		return definition;
	}

	public void setDefinition(String definition) {
		this.definition = definition;
	}

	public HashMap<String, Object> getProperties() {
		return properties;
	}

	public void setProperties(HashMap<String, Object> properties) {
		this.properties = properties;
	}

	public String getPropertiesType() {
		return null;
	}

	@JsonIgnore
	public String getHref(String folder) {
		for (JsonLink link : links) {
			if (link.getRel().equals(folder)) {
				String href = link.getHref();
				if (Strings.isNullOrEmpty(href)) {
					String hreftemplate = link.getHreftemplate();
					href = hreftemplate.contains("{") ? hreftemplate.substring(0, hreftemplate.lastIndexOf("{"))
							: hreftemplate;
				}
				return href;
			}
		}

		return "";
	}

	@JsonIgnore
	public Object getPropertyByName(String name) {
		if (properties != null) {
			return properties.get(name);
		}

		return null;
	}

	@JsonIgnore
	public boolean isDocument() {
		return getBaseType().equals("documents");
	}
	@JsonIgnore public boolean isFolder()
	{
		return getBaseType().equals("folders");
	}

	@JsonIgnore
	public String getBaseType() {
		String linkUrl = "";
		linkUrl = this.getHref(LinkRelation.CANONICAL);
		if (linkUrl.equals("")) {
			linkUrl = this.getHref(LinkRelation.SELF);
		}

		String[] linkParts = linkUrl.split("/");
		String baseType = "";
		if (linkParts.length >= 2) {
			baseType = linkParts[linkParts.length - 2];
		}

		return baseType;
	}

}
