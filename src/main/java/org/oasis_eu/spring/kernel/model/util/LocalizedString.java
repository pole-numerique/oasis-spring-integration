package org.oasis_eu.spring.kernel.model.util;

import com.ibm.icu.util.ULocale;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

public class LocalizedString {
	
	private static final ResourceBundle.Control NO_FALLBACK_CONTROL = ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_DEFAULT);
	
	private final Map<ULocale, String> localizedStrings;
	
	public LocalizedString(String rootValue) {
		this();
		setLocalizedString(ULocale.ROOT.toLocale(), rootValue);
	}
	
	public LocalizedString() {
		this(new HashMap<>());
	}
	
	public LocalizedString(LocalizedString src) {
		this(new HashMap<>(src.localizedStrings));
	}
	
	protected LocalizedString(Map<ULocale, String> values) {
		this.localizedStrings = values;
	}

	public String getLocalizedString(String locale){
		return getLocalizedString(new Locale(locale));
	}
	
	public String getLocalizedString(Locale locale) {
		ULocale uLocale = ULocale.addLikelySubtags(ULocale.forLocale(locale));
		for (Locale candidateLocale : NO_FALLBACK_CONTROL.getCandidateLocales("", uLocale.toLocale())) {
			String value = getValue(ULocale.forLocale(candidateLocale))	;
			if (value != null) {
				return value;
			}
		}
		return null;
	}

	private String getValue(ULocale locale){
		for (Map.Entry<ULocale, String> entry : localizedStrings.entrySet())
		{
			if(entry.getKey().equals(locale)){
			  return entry.getValue();
			}
		}
		return null;
	}

	public void  setLocalizedString(String locale, String localizedValue){
		setLocalizedString(new Locale(locale), localizedValue);
	}
	
	public void setLocalizedString(Locale locale, String localizedValue) {
		localizedStrings.put(ULocale.forLocale(locale), localizedValue);
	}
}
