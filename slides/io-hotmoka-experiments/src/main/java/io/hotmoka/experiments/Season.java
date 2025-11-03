/*
Copyright 2021 Fausto Spoto

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package io.hotmoka.experiments;

import java.time.LocalDate;

public class Season {
	private final LocalDate when;

	public Season(LocalDate when) {
		this.when = when;
	}

	public boolean isSpring() {
		var start = LocalDate.of(when.getYear(), 3, 21);
		var end = LocalDate.of(when.getYear(), 6, 21);
		return !when.isBefore(start) && !when.isAfter(end);
	}

	public boolean isSummer() {
		var start = LocalDate.of(when.getYear(), 6, 21);
		var end = LocalDate.of(when.getYear(), 9, 23);
		return !when.isBefore(start) && !when.isAfter(end);
	}

	public boolean isFall() {
		var start = LocalDate.of(when.getYear(), 9, 23);
		var end = LocalDate.of(when.getYear(), 12, 21);
		return !when.isBefore(start) && !when.isAfter(end);
	}

	public boolean isWinter() {
		var start = LocalDate.of(when.getYear(), 12, 21);
		var end = LocalDate.of(when.getYear(), 3, 21);
		return !when.isAfter(end) || !when.isBefore(start);
	}
}