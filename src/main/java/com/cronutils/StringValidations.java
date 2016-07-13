/*
 * Copyright 2014 jmrozanec
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cronutils;

import com.cronutils.model.field.constraint.FieldConstraints;
import com.cronutils.model.field.value.SpecialChar;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;

import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringValidations {

	private static final String ESCAPED_END = ")\\b";
	private static final String ESCAPED_START = "\\b(";
	private static final SpecialChar[] SPECIAL_CHARS = new SpecialChar[] { SpecialChar.L, SpecialChar.LW, SpecialChar.W };

	private Pattern stringToIntKeysPattern;
	private Pattern numsAndCharsPattern;
	private Pattern lwPattern;

	public StringValidations(FieldConstraints constraints) {
		this.lwPattern = buildLWPattern(constraints.getSpecialChars());
		this.stringToIntKeysPattern = buildStringToIntPattern(constraints.getStringMapping().keySet());
		this.numsAndCharsPattern = Pattern.compile("[#\\?/\\*0-9]");
	}

	@VisibleForTesting
	Pattern buildStringToIntPattern(Set<String> strings) {
		return buildWordsPattern(strings);
	}

	@VisibleForTesting
	public String removeValidChars(String exp) {
		Matcher numsAndCharsMatcher = numsAndCharsPattern.matcher(exp.toUpperCase());
		Matcher stringToIntKeysMatcher = stringToIntKeysPattern.matcher(numsAndCharsMatcher.replaceAll(""));
		Matcher specialWordsMatcher = lwPattern.matcher(stringToIntKeysMatcher.replaceAll(""));
		return specialWordsMatcher.replaceAll("").replaceAll("\\s+", "").replaceAll(",", "").replaceAll("-", "");
	}

	@VisibleForTesting
	Pattern buildLWPattern(Set<SpecialChar> specialChars) {
		Set<String> scs = Sets.newHashSet();
		for (SpecialChar sc : SPECIAL_CHARS) {
			if (specialChars.contains(sc)) {
				scs.add(sc.name());
			}
		}
		return buildWordsPattern(scs);
	}

	@VisibleForTesting
	Pattern buildWordsPattern(Set<String> words) {
		StringBuilder builder = new StringBuilder(ESCAPED_START);
		Iterator<String> iterator = words.iterator();

		if (!iterator.hasNext()) {
			builder.append(ESCAPED_END);
			return Pattern.compile(builder.toString());
		}
		String next = iterator.next();
		builder.append(next);
		while (iterator.hasNext()) {
			builder.append("|");
			builder.append(iterator.next());
		}
		builder.append(ESCAPED_END);
		return Pattern.compile(builder.toString());
	}
}
