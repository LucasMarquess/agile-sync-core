package com.agilesync.utils;

import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.List;

@UtilityClass
public class ObjectUtils {

	public static boolean isNullOrEmpty(Object obj) {
		if (obj == null) {
			return true;
		}
		if (obj instanceof String) {
			return ((String) obj).trim().isEmpty();
		}
		if (obj instanceof Collection) {
			return ((Collection<?>) obj).isEmpty();
		}
		return false;
	}

	public static boolean isNotNullOrEmpty(Object obj) {
		return !isNullOrEmpty(obj);
	}

	public void addIfNotExists(List<String> list, String element) {
		if (!list.contains(element)) {
			list.add(element);
		}
	}

	public BigDecimal truncateToTwoDecimals(BigDecimal value) {
		return value.setScale(2, RoundingMode.DOWN);
	}
}
